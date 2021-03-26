package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.client.screen.TPScreenUtils.tooltipLine
import com.kneelawk.transpositioners.module.RedstoneGateModule
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.buttonCycleEnum
import com.kneelawk.transpositioners.util.RedstoneGateType
import com.kneelawk.transpositioners.util.TranspositionerSide
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import com.kneelawk.transpositioners.util.IconUtils.redstoneGateType as redstoneGateTypeI
import com.kneelawk.transpositioners.util.IconUtils.transpositionerSide as transpositionerSideI
import com.kneelawk.transpositioners.util.TooltipUtils.redstoneGateSide as redstoneGateSideT
import com.kneelawk.transpositioners.util.TooltipUtils.redstoneGateType as redstoneGateTypeT

class RedstoneGateScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, override val module: RedstoneGateModule
) : SyncedGuiDescription(TPScreenHandlers.REDSTONE_GATE_TYPE, syncId, playerInventory), ModuleScreenHandler {
    companion object {
        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            RedstoneGateScreenHandler::class.java,
            TPConstants.str("redstone_gate_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val ID_GATE_TYPE_CHANGE = NET_PARENT.idData("GATE_TYPE_CHANGE")
            .setServerReceiver { module.updateGateType(RedstoneGateType.byId(it.readByte().toInt())) }
        private val ID_GATE_SIDE_CHANGE = NET_PARENT.idData("GATE_SIDE_CHANGE")
            .setServerReceiver { module.updateGateSide(TranspositionerSide.byId(it.readByte().toInt())) }
    }

    private val gateType: WScalableButton
    private val gateSide: WScalableButton

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WPlainPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 2 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        gateType = WScalableButton(
            icon = redstoneGateTypeI(module.gateType)
        )
        root.add(gateType, 3 * 18 + 9, 18)
        gateType.tooltip = listOf(tooltipLine(redstoneGateTypeT(module.gateType)))
        gateType.onClick = { button ->
            val gateType = buttonCycleEnum(module.gateType, button)
            ID_GATE_TYPE_CHANGE.sendToServer(this) { it.writeByte(gateType.id) }
        }

        gateSide = WScalableButton(icon = transpositionerSideI(module.gateSide))
        root.add(gateSide, 4 * 18 + 9, 18)
        gateSide.tooltip = listOf(tooltipLine(redstoneGateSideT(module.gateSide)))
        gateSide.onClick = { button ->
            val gateSide = buttonCycleEnum(module.gateSide, button)
            ID_GATE_SIDE_CHANGE.sendToServer(this) { it.writeByte(gateSide.id) }
        }

        root.validate(this)
    }

    fun s2cReceiveGateTypeChange(type: RedstoneGateType) {
        gateType.icon = redstoneGateTypeI(type)
        gateType.tooltip = listOf(tooltipLine(redstoneGateTypeT(type)))
    }

    fun s2cReceiveGateSideChange(side: TranspositionerSide) {
        gateSide.icon = transpositionerSideI(side)
        gateSide.tooltip = listOf(tooltipLine(redstoneGateSideT(side)))
    }
}