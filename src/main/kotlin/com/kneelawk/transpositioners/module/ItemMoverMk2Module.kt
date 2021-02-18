package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import com.kneelawk.transpositioners.TranspositionersConstants
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
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class ItemMoverMk2Module(context: ModuleContext, path: ModulePath, initialDirection: MovementDirection) :
    AbstractTranspositionerModule(Type, context, path), MoverModule, ExtendedScreenHandlerFactory {
    companion object {
        const val MAX_STACK_SIZE = 1

        private val NET_PARENT = TranspositionerModule.NET_ID.subType(
            ItemMoverMk2Module::class.java,
            TranspositionersConstants.str("item_mover_mk2_module")
        )

        private val ID_DIRECTION_CHANGE =
            NET_PARENT.idData("DIRECTION_CHANGE").setReceiver(ItemMoverMk2Module::receiveChangeDirection)
    }

    var direction = initialDirection
        private set

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

    override fun move() {
        val extract = getItemExtractable(attachmentPos.offset(facing.opposite), facing.opposite)
        val insert = getItemInsertable(attachmentPos, facing)

        val extractedSim = extract.attemptAnyExtraction(MAX_STACK_SIZE, Simulation.SIMULATE)
        if (!extractedSim.isEmpty) {
            val leftOverSim = insert.attemptInsertion(extractedSim, Simulation.SIMULATE)
            val amount = extractedSim.count - leftOverSim.count

            if (amount != 0) {
                val leftOver =
                    insert.attemptInsertion(
                        extract.attemptAnyExtraction(amount, Simulation.ACTION),
                        Simulation.ACTION
                    )
                assert(leftOver.isEmpty) { "leftOver: $leftOver" }
            }
        }
    }

    private fun getItemInsertable(pos: BlockPos, direction: Direction): ItemInsertable {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getItemExtractable(pos: BlockPos, direction: Direction): ItemExtractable {
        return ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return ItemMoverMk2ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return TranspositionersConstants.item("item_mover_module_mk2")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        TranspositionerModule.writeModulePath(this, buf)
    }

    override fun writeToTag(tag: CompoundTag) {
        tag.putByte("direction", direction.ordinal.toByte())
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

            return ItemMoverMk2Module(context, path, direction)
        }

        override fun newInstance(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack
        ): ItemMoverMk2Module {
            return ItemMoverMk2Module(context, path, MovementDirection.FORWARD)
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
                tooltip += TranspositionersConstants.tooltip(
                    "direction",
                    TranspositionersConstants.tooltip(direction.name.toLowerCase()).apply {
                        when (direction) {
                            MovementDirection.FORWARD -> formatted(Formatting.GREEN)
                            MovementDirection.BACKWARD -> formatted(Formatting.BLUE)
                        }
                    })
            }
        }
    }
}