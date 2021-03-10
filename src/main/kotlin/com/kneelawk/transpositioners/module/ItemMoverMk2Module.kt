package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.*
import alexiil.mc.lib.attributes.item.impl.EmptyFixedItemInv
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.screen.ItemMoverMk2ScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class ItemMoverMk2Module(
    context: ModuleContext,
    path: ModulePath,
    initialDirection: MovementDirection,
    initialInsertionSide: Direction,
    initialExtractionSide: Direction,
    val filters: ModuleInventory<ItemFilterModule>
) :
    AbstractModule(Type, context, path), MoverModule, ExtendedScreenHandlerFactory {
    companion object {
        const val MAX_STACK_SIZE = 8

        private val NET_PARENT = Module.NET_ID.subType(
            ItemMoverMk2Module::class.java,
            TPConstants.str("item_mover_mk2_module")
        )

        private val ID_DIRECTION_CHANGE =
            NET_PARENT.idData("DIRECTION_CHANGE").setReceiver(ItemMoverMk2Module::receiveChangeDirection)
        private val ID_INSERTION_SIDE_CHANGE =
            NET_PARENT.idData("INSERTION_SIDE_CHANGE").setReceiver(ItemMoverMk2Module::receiveChangeInsertionSide)
        private val ID_EXTRACTION_SIDE_CHANGE =
            NET_PARENT.idData("EXTRACTION_SIDE_CHANGE").setReceiver(ItemMoverMk2Module::receiveChangeExtractionSide)
    }

    var direction = initialDirection
        private set
    var insertionSide = initialInsertionSide
        private set
    var extractionSide = initialExtractionSide
        private set

    private val ignoreStacks = mutableSetOf<StackContainer>()

    fun updateDirection(newDirection: MovementDirection) {
        direction = newDirection
        sendChangeDirection(newDirection)
    }

    private fun sendChangeDirection(newDirection: MovementDirection) {
        for (con in CoreMinecraftNetUtil.getPlayersWatching(world, attachmentPos)) {
            ID_DIRECTION_CHANGE.send(con, this) { _, buf, ctx ->
                ctx.assertServerSide()
                buf.writeByte(newDirection.ordinal)
            }
        }
    }

    private fun receiveChangeDirection(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertClientSide()
        val newDirection = MovementDirection.values()[buf.readByte().toInt()]
        direction = newDirection

        ModuleUtils.screenHandler<ItemMoverMk2ScreenHandler>(ctx, this) { it.s2cReceiveDirectionChange(newDirection) }
    }

    fun updateInsertionSide(newInsertionSide: Direction) {
        insertionSide = newInsertionSide
        sendChangeInsertionSide(newInsertionSide)
    }

    private fun sendChangeInsertionSide(newInsertionSide: Direction) {
        for (con in CoreMinecraftNetUtil.getPlayersWatching(world, attachmentPos)) {
            ID_INSERTION_SIDE_CHANGE.send(con, this) { _, buf, ctx ->
                ctx.assertServerSide()
                buf.writeByte(newInsertionSide.id)
            }
        }
    }

    private fun receiveChangeInsertionSide(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertClientSide()
        val newSide = Direction.byId(buf.readByte().toInt())
        insertionSide = newSide

        ModuleUtils.screenHandler<ItemMoverMk2ScreenHandler>(ctx, this) { it.s2cReceiveInsertionSideChange(newSide) }
    }

    fun updateExtractionSide(newExtractionSide: Direction) {
        extractionSide = newExtractionSide
        sendChangeExtractionSide(newExtractionSide)
    }

    private fun sendChangeExtractionSide(newExtractionSide: Direction) {
        for (con in CoreMinecraftNetUtil.getPlayersWatching(world, attachmentPos)) {
            ID_EXTRACTION_SIDE_CHANGE.send(con, this) { _, buf, ctx ->
                ctx.assertServerSide()
                buf.writeByte(newExtractionSide.id)
            }
        }
    }

    private fun receiveChangeExtractionSide(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertClientSide()
        val newSide = Direction.byId(buf.readByte().toInt())
        extractionSide = newSide

        ModuleUtils.screenHandler<ItemMoverMk2ScreenHandler>(ctx, this) { it.s2cReceiveExtractionSideChange(newSide) }
    }

    override fun move() {
        ignoreStacks.clear()

        val insert = getItemInsertable(
            when (direction) {
                MovementDirection.FORWARD -> attachmentPos
                MovementDirection.BACKWARD -> attachmentPos.offset(facing.opposite)
            }, insertionSide.opposite
        )
        // TODO: Use ItemInsertableFilter
        val extractInv = getFixedItemInv(
            when (direction) {
                MovementDirection.FORWARD -> attachmentPos.offset(facing.opposite)
                MovementDirection.BACKWARD -> attachmentPos
            }, extractionSide.opposite
        )

        if (extractInv != EmptyFixedItemInv.INSTANCE) {
            var remaining = MAX_STACK_SIZE
            for (slot in 0 until extractInv.slotCount) {
                val extract = extractInv.getSlot(slot)
                remaining = attemptTransfer(remaining, extract, insert, ignoreStacks)
                if (remaining == 0) break
            }
        } else {
            val extract = getItemExtractable(
                when (direction) {
                    MovementDirection.FORWARD -> attachmentPos.offset(facing.opposite)
                    MovementDirection.BACKWARD -> attachmentPos
                }, extractionSide.opposite
            )

            attemptTransfer(MAX_STACK_SIZE, extract, insert, ignoreStacks)
        }
    }

    private fun attemptTransfer(
        remaining: Int,
        extract: ItemExtractable,
        insert: ItemInsertable,
        ignore: MutableSet<StackContainer>
    ): Int {
        val extractedSim = extract.attemptAnyExtraction(remaining, Simulation.SIMULATE)
        if (!extractedSim.isEmpty && !ignore.contains(StackContainer.of(extractedSim))) {
            val leftOverSim = insert.attemptInsertion(extractedSim, Simulation.SIMULATE)
            val amount = extractedSim.count - leftOverSim.count

            if (amount != 0) {
                val leftOver =
                    insert.attemptInsertion(
                        extract.attemptAnyExtraction(amount, Simulation.ACTION),
                        Simulation.ACTION
                    )
                assert(leftOver.isEmpty) { "leftOver: $leftOver" }
            } else {
                // If an inventory won't accept a kind of item, don't try and insert it a second time
                ignore.add(StackContainer.of(extractedSim))
            }

            return remaining - amount
        }

        return remaining
    }

    private fun getItemInsertable(pos: BlockPos, direction: Direction): ItemInsertable {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getFixedItemInv(pos: BlockPos, direction: Direction): FixedItemInv {
        return ItemAttributes.FIXED_INV.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getItemExtractable(pos: BlockPos, direction: Direction): ItemExtractable {
        return ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return ItemMoverMk2ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return TPConstants.item("item_mover_module_mk2")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun writeToTag(tag: CompoundTag) {
        tag.putByte("direction", direction.ordinal.toByte())
        tag.putByte("insertionSide", insertionSide.id.toByte())
        tag.putByte("extractionSide", extractionSide.id.toByte())
        tag.put("filters", filters.getTags())
    }

    override fun getModule(index: Int): Module? {
        return filters.getModule(index)
    }

    object Type : ModuleType<ItemMoverMk2Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: CompoundTag
        ): ItemMoverMk2Module {
            val direction = if (tag.contains("direction")) MovementDirection.values()[tag.getByte("direction").toInt()
                .coerceIn(0, 1)] else MovementDirection.FORWARD
            val insertionSide = if (tag.contains("insertionSide")) {
                Direction.byId(tag.getByte("insertionSide").toInt())
            } else {
                when (context) {
                    is ModuleContext.Configurator -> Direction.UP
                    is ModuleContext.Entity -> context.facing.opposite
                }
            }
            val extractionSide = if (tag.contains("extractionSide")) {
                Direction.byId(tag.getByte("extractionSide").toInt())
            } else {
                when (context) {
                    is ModuleContext.Configurator -> Direction.DOWN
                    is ModuleContext.Entity -> context.facing
                }
            }

            val filters = ModuleInventory(2, context, path, TPModules.ITEM_FILTERS)
            if (tag.contains("filters")) {
                filters.readTags(tag.getList("filters", 10))
            }

            return ItemMoverMk2Module(context, path, direction, insertionSide, extractionSide, filters)
        }

        override fun newInstance(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack
        ): ItemMoverMk2Module {
            return ItemMoverMk2Module(
                context, path, MovementDirection.FORWARD, when (context) {
                    is ModuleContext.Configurator -> Direction.UP
                    is ModuleContext.Entity -> context.facing.opposite
                }, when (context) {
                    is ModuleContext.Configurator -> Direction.DOWN
                    is ModuleContext.Entity -> context.facing
                }, ModuleInventory(2, context, path, TPModules.ITEM_FILTERS)
            )
        }

        override fun appendTooltip(
            stack: ItemStack,
            world: World?,
            tooltip: MutableList<Text>,
            tooltipContext: TooltipContext,
            moduleData: CompoundTag
        ) {
            if (moduleData.contains("direction")) {
                val direction = MovementDirection.values()[moduleData.getByte("direction").toInt().coerceIn(0, 1)]
                tooltip += ModuleUtils.movementDirectionTooltip(direction)
            }

            if (moduleData.contains("insertionSide")) {
                val insertionSide = Direction.byId(moduleData.getByte("insertionSide").toInt())
                tooltip += TPConstants.tooltip(
                    "insertion_side",
                    ModuleUtils.directionTooltip(insertionSide)
                )
            }

            if (moduleData.contains("extractionSide")) {
                val extractionSide = Direction.byId(moduleData.getByte("extractionSide").toInt())
                tooltip += TPConstants.tooltip(
                    "extraction_side",
                    ModuleUtils.directionTooltip(extractionSide)
                )
            }

            if (moduleData.contains("filters")) {
                val filters = moduleData.getList("filters", 10)
                if (!filters.isEmpty()) {
                    tooltip += TPConstants.tooltip("filters", filters.size)
                }
            }
        }
    }

    class StackContainer private constructor(val stack: ItemStack) {
        companion object {
            fun of(stack: ItemStack): StackContainer {
                if (stack.isEmpty)
                    throw IllegalArgumentException("Contained stack cannot be empty")
                return StackContainer(stack.copy())
            }
        }

        override fun equals(other: Any?): Boolean {
            return when {
                this === other -> true
                javaClass != other?.javaClass -> false
                else -> ItemStackUtil.areEqualIgnoreAmounts(stack, (other as StackContainer).stack)
            }
        }

        override fun hashCode(): Int {
            // shouldn't be needed because we shouldn't be keeping track of empty stacks anyways, but just in case
            if (stack.isEmpty) return 0

            var result = stack.item.hashCode()
            result = 31 * result + stack.tag.hashCode()
            return result
        }
    }
}