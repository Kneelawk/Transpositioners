package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ConstantItemFilter
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.tt
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.net.ModuleDataPacketHandler
import com.kneelawk.transpositioners.screen.RedstoneGateScreenHandler
import com.kneelawk.transpositioners.util.RedstoneGateType
import com.kneelawk.transpositioners.util.TooltipUtils.redstoneGateSide
import com.kneelawk.transpositioners.util.TooltipUtils.redstoneGateType
import com.kneelawk.transpositioners.util.TranspositionerSide
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
import net.minecraft.world.World

class RedstoneGateModule(
    context: ModuleContext, path: ModulePath, initialGateType: RedstoneGateType, initialGateSide: TranspositionerSide,
    private var previousRedstone: Boolean
) : AbstractModule(Type, context, path), ItemGateModule, ExtendedScreenHandlerFactory {
    companion object {
        private val NET_PARENT = Module.NET_ID.subType(
            RedstoneGateModule::class.java,
            TPConstants.str("redstone_gate_module")
        )

        private val GATE_TYPE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("GATE_TYPE_CHANGE"),
            RedstoneGateScreenHandler::class, { it.writeByte(gateType.id) },
            { gateType = RedstoneGateType.byId(it.readByte().toInt()) },
            { it.s2cReceiveGateTypeChange(gateType) })
        private val GATE_SIDE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("GATE_SIDE_CHANGE"),
            RedstoneGateScreenHandler::class, { it.writeByte(gateSide.id) },
            { gateSide = TranspositionerSide.byId(it.readByte().toInt()) },
            { it.s2cReceiveGateSideChange(gateSide) })
    }

    var gateType = initialGateType
        private set
    var gateSide = initialGateSide
        private set

    fun updateGateType(type: RedstoneGateType) {
        gateType = type
        GATE_TYPE_CHANGE.sendToClients(this)
        markDirty()
    }

    fun updateGateSide(side: TranspositionerSide) {
        gateSide = side
        GATE_SIDE_CHANGE.sendToClients(this)
        markDirty()
    }

    override fun getItemFilter(): ItemFilter {
        val pos = when (gateSide) {
            TranspositionerSide.FRONT -> frontPos
            TranspositionerSide.BACK  -> backPos
        }

        val redstoneValue = world.isReceivingRedstonePower(pos)

        val res = when (gateType) {
            RedstoneGateType.REDSTONE_DISABLE      -> false
            RedstoneGateType.REDSTONE_HIGH         -> redstoneValue
            RedstoneGateType.REDSTONE_LOW          -> !redstoneValue
            RedstoneGateType.REDSTONE_RISING_EDGE  -> redstoneValue && !previousRedstone
            RedstoneGateType.REDSTONE_FALLING_EDGE -> !redstoneValue && previousRedstone
        }

        if (previousRedstone != redstoneValue) {
            previousRedstone = redstoneValue

            // NBT updated with previousRedstone change
            markDirty()
        }

        return if (res) {
            ConstantItemFilter.ANYTHING
        } else {
            ConstantItemFilter.NOTHING
        }
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return RedstoneGateScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return tt(TPItems.REDSTONE_GATE_MODULE.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun writeToTag(tag: NbtCompound) {
        tag.putByte("gate_type", gateType.id.toByte())
        tag.putByte("gate_side", gateSide.id.toByte())
        tag.putBoolean("previous_redstone", previousRedstone)
    }

    object Type : ModuleType<RedstoneGateModule> {
        override fun readFromTag(
            context: ModuleContext, path: ModulePath, stack: ItemStack, tag: NbtCompound
        ): RedstoneGateModule {
            val gateType = if (tag.contains("gate_type")) {
                RedstoneGateType.byId(tag.getByte("gate_type").toInt())
            } else {
                RedstoneGateType.REDSTONE_HIGH
            }

            val gateSide = if (tag.contains("gate_side")) {
                TranspositionerSide.byId(tag.getByte("gate_side").toInt())
            } else {
                TranspositionerSide.FRONT
            }

            val lastRedstone = if (tag.contains("last_redstone")) {
                tag.getBoolean("last_redstone")
            } else {
                false
            }

            return RedstoneGateModule(context, path, gateType, gateSide, lastRedstone)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): RedstoneGateModule {
            return RedstoneGateModule(context, path, RedstoneGateType.REDSTONE_HIGH, TranspositionerSide.FRONT, false)
        }

        override fun appendTooltip(
            stack: ItemStack, world: World?, tooltip: MutableList<Text>, tooltipContext: TooltipContext,
            moduleData: NbtCompound
        ) {
            if (moduleData.contains("gate_type")) {
                tooltip += redstoneGateType(RedstoneGateType.byId(moduleData.getByte("gate_type").toInt()))
            }

            if (moduleData.contains("gate_side")) {
                tooltip += redstoneGateSide(TranspositionerSide.byId(moduleData.getByte("gate_side").toInt()))
            }
        }
    }
}