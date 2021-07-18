package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import alexiil.mc.lib.attributes.item.filter.ConstantItemFilter
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import alexiil.mc.lib.attributes.item.impl.EmptyFixedItemInv
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.tt
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.net.ModuleDataPacketHandler
import com.kneelawk.transpositioners.screen.ItemMoverMk2ScreenHandler
import com.kneelawk.transpositioners.util.ExactStackContainer
import com.kneelawk.transpositioners.util.MovementDirection
import com.kneelawk.transpositioners.util.TooltipUtils.extractionSide
import com.kneelawk.transpositioners.util.TooltipUtils.insertionSide
import com.kneelawk.transpositioners.util.TooltipUtils.movementDirection
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
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
    val gates: ModuleInventory<ItemGateModule>
) :
    AbstractModule(Type, context, path), MoverModule, ExtendedScreenHandlerFactory {
    companion object {
        const val MAX_STACK_SIZE = 8

        private val NET_PARENT = Module.NET_ID.subType(
            ItemMoverMk2Module::class.java,
            TPConstants.str("item_mover_mk2_module")
        )

        private val DIRECTION_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("DIRECTION_CHANGE"),
            ItemMoverMk2ScreenHandler::class, { it.writeByte(direction.id) },
            { direction = MovementDirection.byId(it.readByte().toInt()) },
            { it.s2cReceiveDirectionChange(direction) })
        private val INSERTION_SIDE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("INSERTION_SIDE_CHANGE"),
            ItemMoverMk2ScreenHandler::class, { it.writeByte(insertionSide.id) },
            { insertionSide = Direction.byId(it.readByte().toInt()) },
            { it.s2cReceiveInsertionSideChange(insertionSide) })
        private val EXTRACTION_SIDE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("EXTRACTION_SIDE_CHANGE"),
            ItemMoverMk2ScreenHandler::class, { it.writeByte(extractionSide.id) },
            { extractionSide = Direction.byId(it.readByte().toInt()) },
            { it.s2cReceiveExtractionSideChange(extractionSide) })
    }

    var direction = initialDirection
        private set
    var insertionSide = initialInsertionSide
        private set
    var extractionSide = initialExtractionSide
        private set

    private val ignoreStacks = mutableSetOf<ExactStackContainer>()

    fun updateDirection(newDirection: MovementDirection) {
        direction = newDirection
        DIRECTION_CHANGE.sendToClients(this)
    }

    fun updateInsertionSide(newInsertionSide: Direction) {
        insertionSide = newInsertionSide
        INSERTION_SIDE_CHANGE.sendToClients(this)
    }

    fun updateExtractionSide(newExtractionSide: Direction) {
        extractionSide = newExtractionSide
        EXTRACTION_SIDE_CHANGE.sendToClients(this)
    }

    fun getInsertionPos() = when (direction) {
        MovementDirection.FORWARD  -> frontPos
        MovementDirection.BACKWARD -> backPos
    }

    fun getExtractionPos() = when (direction) {
        MovementDirection.FORWARD  -> backPos
        MovementDirection.BACKWARD -> frontPos
    }

    override fun move() {
        ignoreStacks.clear()

        val insert = getItemInsertable(getInsertionPos(), insertionSide.opposite)
        val extractInv = getFixedItemInv(getExtractionPos(), extractionSide.opposite)

//        val itemFilter = AggregateItemFilter.allOf(
//            gates.moduleStream().filter { it != null }.map { it!!.getItemFilter() }.collect(Collectors.toList())
//        )
        val itemFilter = gates.getModule(0)?.getItemFilter() ?: ConstantItemFilter.ANYTHING

        if (extractInv != EmptyFixedItemInv.INSTANCE) {
            var remaining = MAX_STACK_SIZE
            for (slot in 0 until extractInv.slotCount) {
                val extract = extractInv.getSlot(slot)
                remaining = attemptTransfer(remaining, extract, insert, ignoreStacks, itemFilter)
                if (remaining == 0) break
            }
        } else {
            val extract = getItemExtractable(getExtractionPos(), extractionSide.opposite)

            attemptTransfer(MAX_STACK_SIZE, extract, insert, ignoreStacks, itemFilter)
        }
    }

    private fun attemptTransfer(
        remaining: Int,
        extract: ItemExtractable,
        insert: ItemInsertable,
        ignore: MutableSet<ExactStackContainer>,
        filter: ItemFilter
    ): Int {
        val extractedSim = extract.attemptExtraction(filter, remaining, Simulation.SIMULATE)
        if (!extractedSim.isEmpty && !ignore.contains(ExactStackContainer.of(extractedSim))) {
            val leftOverSim = insert.attemptInsertion(extractedSim, Simulation.SIMULATE)
            val amount = extractedSim.count - leftOverSim.count

            if (amount != 0) {
                val leftOver =
                    insert.attemptInsertion(
                        extract.attemptExtraction(filter, amount, Simulation.ACTION),
                        Simulation.ACTION
                    )
                assert(leftOver.isEmpty) { "leftOver: $leftOver" }
            } else {
                // If an inventory won't accept a kind of item, don't try and insert it a second time
                ignore.add(ExactStackContainer.of(extractedSim))
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
        return tt(TPItems.ITEM_MOVER_MODULE_MK2.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun writeToTag(tag: NbtCompound) {
        tag.putByte("direction", direction.ordinal.toByte())
        tag.putByte("insertionSide", insertionSide.id.toByte())
        tag.putByte("extractionSide", extractionSide.id.toByte())
        tag.put("gates", gates.toNbtList())
    }

    override fun getModule(index: Int): Module? {
        return gates.getModule(index)
    }

    object Type : ModuleType<ItemMoverMk2Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: NbtCompound
        ): ItemMoverMk2Module {
            val direction = if (tag.contains("direction")) MovementDirection.values()[tag.getByte("direction").toInt()
                .coerceIn(0, 1)] else MovementDirection.FORWARD
            val insertionSide = if (tag.contains("insertionSide")) {
                Direction.byId(tag.getByte("insertionSide").toInt())
            } else {
                when (context) {
                    is ModuleContext.Configurator -> Direction.UP
                    is ModuleContext.Entity       -> context.facing.opposite
                }
            }
            val extractionSide = if (tag.contains("extractionSide")) {
                Direction.byId(tag.getByte("extractionSide").toInt())
            } else {
                when (context) {
                    is ModuleContext.Configurator -> Direction.DOWN
                    is ModuleContext.Entity       -> context.facing
                }
            }

            val gates = ModuleInventory(1, context, path, TPModules.ITEM_GATES)
            if (tag.contains("gates")) {
                gates.readNbtList(tag.getList("gates", 10))
            }

            return ItemMoverMk2Module(context, path, direction, insertionSide, extractionSide, gates)
        }

        override fun newInstance(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack
        ): ItemMoverMk2Module {
            return ItemMoverMk2Module(
                context, path, MovementDirection.FORWARD, when (context) {
                    is ModuleContext.Configurator -> Direction.UP
                    is ModuleContext.Entity       -> context.facing.opposite
                }, when (context) {
                    is ModuleContext.Configurator -> Direction.DOWN
                    is ModuleContext.Entity       -> context.facing
                }, ModuleInventory(1, context, path, TPModules.ITEM_GATES)
            )
        }

        override fun appendTooltip(
            stack: ItemStack,
            world: World?,
            tooltip: MutableList<Text>,
            tooltipContext: TooltipContext,
            moduleData: NbtCompound
        ) {
            if (moduleData.contains("direction")) {
                val direction = MovementDirection.values()[moduleData.getByte("direction").toInt().coerceIn(0, 1)]
                tooltip += movementDirection(direction)
            }

            if (moduleData.contains("insertionSide")) {
                val insertionSide = Direction.byId(moduleData.getByte("insertionSide").toInt())
                tooltip += insertionSide(insertionSide)
            }

            if (moduleData.contains("extractionSide")) {
                val extractionSide = Direction.byId(moduleData.getByte("extractionSide").toInt())
                tooltip += extractionSide(extractionSide)
            }

            if (moduleData.contains("gates")) {
                val gates = moduleData.getList("gates", 10)
                if (!gates.isEmpty()) {
                    tooltip += TPConstants.tooltip("gates", gates.size)
                }
            }
        }
    }

}