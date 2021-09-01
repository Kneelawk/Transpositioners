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
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.net.ModuleDataPacketHandler
import com.kneelawk.transpositioners.screen.ItemMoverMk3ScreenHandler
import com.kneelawk.transpositioners.util.ExactStackContainer
import com.kneelawk.transpositioners.util.MovementDirection
import com.kneelawk.transpositioners.util.TooltipUtils
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

class ItemMoverMk3Module(
    context: ModuleContext, path: ModulePath, initialDirection: MovementDirection, initialInsertionSide: Direction,
    initialExtractionSide: Direction, initialStackSize: Int, initialTicksPerMove: Int,
    val gates: ModuleInventory<ItemGateModule>, private var lastMove: Long
) : AbstractModule(Type, context, path), MoverModule, ExtendedScreenHandlerFactory {
    companion object {
        const val MIN_STACK_SIZE = 1
        const val MAX_STACK_SIZE = 256

        const val DEFAULT_STACK_SIZE = 8
        const val DEFAULT_TICKS_PER_MOVE = 40

        private val NET_PARENT = Module.NET_ID.subType(
            ItemMoverMk3Module::class.java,
            TPConstants.str("item_mover_mk3_module")
        )

        private val DIRECTION_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("DIRECTION_CHANGE"),
            ItemMoverMk3ScreenHandler::class, { it.writeByte(direction.id) },
            { direction = MovementDirection.byId(it.readByte().toInt()) },
            { it.s2cReceiveDirectionChange(direction) })
        private val INSERTION_SIDE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("INSERTION_SIDE_CHANGE"),
            ItemMoverMk3ScreenHandler::class, { it.writeByte(insertionSide.id) },
            { insertionSide = Direction.byId(it.readByte().toInt()) },
            { it.s2cReceiveInsertionSideChange(insertionSide) })
        private val EXTRACTION_SIDE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("EXTRACTION_SIDE_CHANGE"),
            ItemMoverMk3ScreenHandler::class, { it.writeByte(extractionSide.id) },
            { extractionSide = Direction.byId(it.readByte().toInt()) },
            { it.s2cReceiveExtractionSideChange(extractionSide) })
        private val STACK_SIZE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("STACK_SIZE_CHANGE"),
            ItemMoverMk3ScreenHandler::class, { it.writeVarUnsignedInt(stackSize) },
            { stackSize = it.readVarUnsignedInt() },
            { it.s2cReceiveStackSizeChange(stackSize) }
        )
        private val MOVES_PER_SECOND_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("MOVES_PER_SECOND_CHANGE"),
            ItemMoverMk3ScreenHandler::class, { it.writeVarUnsignedInt(ticksPerMove) },
            { ticksPerMove = it.readVarUnsignedInt() },
            { it.s2cReceiveMovesPerSecondChange(ticksPerMove) }
        )
    }

    var direction = initialDirection
        private set
    var insertionSide = initialInsertionSide
        private set
    var extractionSide = initialExtractionSide
        private set
    var stackSize = initialStackSize.coerceIn(MIN_STACK_SIZE, MAX_STACK_SIZE)
        private set
    var ticksPerMove = initialTicksPerMove
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

    fun updateStackSize(newStackSize: Int) {
        stackSize = newStackSize.coerceIn(MIN_STACK_SIZE, MAX_STACK_SIZE)
        STACK_SIZE_CHANGE.sendToClients(this)
    }

    fun updateTicksPerMove(newTicksPerMove: Int) {
        ticksPerMove = newTicksPerMove.coerceAtLeast(1)
        MOVES_PER_SECOND_CHANGE.sendToClients(this)
    }

    fun getInsertionPos() = when (direction) {
        MovementDirection.FORWARD -> frontPos
        MovementDirection.BACKWARD -> backPos
    }

    fun getExtractionPos() = when (direction) {
        MovementDirection.FORWARD -> backPos
        MovementDirection.BACKWARD -> frontPos
    }

    override fun move() {
        val time = world.time

        if (time - lastMove >= ticksPerMove) {
            lastMove = time

            ignoreStacks.clear()

            val insert = getItemInsertable(getInsertionPos(), insertionSide.opposite)
            val extractInv = getFixedItemInv(getExtractionPos(), extractionSide.opposite)

            val itemFilter = gates.getModule(0)?.getItemFilter() ?: ConstantItemFilter.ANYTHING

            if (extractInv != EmptyFixedItemInv.INSTANCE) {
                var remaining = stackSize
                for (slot in 0 until extractInv.slotCount) {
                    val extract = extractInv.getSlot(slot)
                    remaining = attemptTransfer(remaining, extract, insert, ignoreStacks, itemFilter)
                    if (remaining == 0) break
                }
            } else {
                val extract = getItemExtractable(getExtractionPos(), extractionSide.opposite)

                attemptTransfer(stackSize, extract, insert, ignoreStacks, itemFilter)
            }
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
        return ItemMoverMk3ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return TPConstants.tt(TPItems.ITEM_MOVER_MODULE_MK3.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun writeToTag(tag: NbtCompound) {
        tag.putByte("direction", direction.ordinal.toByte())
        tag.putByte("insertionSide", insertionSide.id.toByte())
        tag.putByte("extractionSide", extractionSide.id.toByte())
        tag.putInt("stackSize", stackSize)
        tag.putInt("ticksPerMove", ticksPerMove)
        tag.put("gates", gates.toNbtList())
        tag.putLong("lastMove", lastMove)
    }

    override fun getModule(index: Int): Module? {
        return gates.getModule(index)
    }

    object Type : ModuleType<ItemMoverMk3Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: NbtCompound
        ): ItemMoverMk3Module {
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

            val stackSize = if (tag.contains("stackSize")) tag.getInt("stackSize") else DEFAULT_STACK_SIZE

            val ticksPerMove = if (tag.contains("ticksPerMove")) tag.getInt("ticksPerMove") else DEFAULT_TICKS_PER_MOVE

            val gates = ModuleInventory(1, context, path, TPModules.ITEM_GATES)
            if (tag.contains("gates")) {
                gates.readNbtList(tag.getList("gates", 10))
            }

            val lastMove = if (tag.contains("lastMove")) tag.getLong("lastMove") else 0L

            return ItemMoverMk3Module(
                context, path, direction, insertionSide, extractionSide, stackSize, ticksPerMove, gates, lastMove
            )
        }

        override fun newInstance(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack
        ): ItemMoverMk3Module {
            return ItemMoverMk3Module(
                context, path, MovementDirection.FORWARD, when (context) {
                    is ModuleContext.Configurator -> Direction.UP
                    is ModuleContext.Entity -> context.facing.opposite
                }, when (context) {
                    is ModuleContext.Configurator -> Direction.DOWN
                    is ModuleContext.Entity -> context.facing
                }, DEFAULT_STACK_SIZE, DEFAULT_TICKS_PER_MOVE, ModuleInventory(1, context, path, TPModules.ITEM_GATES), 0L
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
                tooltip += TooltipUtils.movementDirection(direction)
            }

            if (moduleData.contains("insertionSide")) {
                val insertionSide = Direction.byId(moduleData.getByte("insertionSide").toInt())
                tooltip += TooltipUtils.insertionSide(insertionSide)
            }

            if (moduleData.contains("extractionSide")) {
                val extractionSide = Direction.byId(moduleData.getByte("extractionSide").toInt())
                tooltip += TooltipUtils.extractionSide(extractionSide)
            }

            if (moduleData.contains("stackSize")) {
                val stackSize = moduleData.getInt("stackSize")
                tooltip += TooltipUtils.stackSize(stackSize)
            }

            if (moduleData.contains("ticksPerMove")) {
                val ticksPerMove = moduleData.getInt("ticksPerMove")
                tooltip += TooltipUtils.ticksPerMove(ticksPerMove)
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