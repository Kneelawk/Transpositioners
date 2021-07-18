package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.client.screen.TPScreenUtils.tooltipLine
import com.kneelawk.transpositioners.module.ItemGateMk1Module
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.cycleEnum
import com.kneelawk.transpositioners.util.ListGateType
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager
import com.kneelawk.transpositioners.util.IconUtils.listGateType as listGateTypeI
import com.kneelawk.transpositioners.util.TooltipUtils.listGateType as listGateTypeT

class ItemGateMk1ScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    override val module: ItemGateMk1Module
) : SyncedGuiDescription(
    TPScreenHandlers.ITEM_GATE_MK1_TYPE,
    syncId,
    playerInventory
), ModuleScreenHandler {
    companion object {
        val LOGGER = LogManager.getLogger()

        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            ItemGateMk1ScreenHandler::class.java,
            str("item_gate_mk1_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val ID_GATE_TYPE_CHANGE = NET_PARENT.idData("GATE_TYPE_CHANGE")
            .setServerReceiver { module.updateGateType(ListGateType.byId(it.readByte().toInt())) }
    }

    private val gateType: WScalableButton

    init {
        setTitleAlignment(HorizontalAlignment.CENTER)

        val root = WPlainPanel()
        setRootPanel(root)
        root.insets = Insets.ROOT_PANEL

        root.add(createPlayerInventoryPanel(), 0, 4 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        val slots = WItemSlot.of(module.items, 0, 3, 3)
        root.add(slots, 3 * 18, 18)

        gateType = WScalableButton(
            icon = listGateTypeI(module.gateType)
        )
        root.add(gateType, 7 * 18, 2 * 18)
        gateType.tooltip = listOf(tooltipLine(listGateTypeT(module.gateType)))
        gateType.onClick = {
            val gateType = cycleEnum(module.gateType)
            ID_GATE_TYPE_CHANGE.sendToServer(this) { it.writeByte(gateType.id) }
        }

        root.validate(this)
    }

    override fun onSlotClick(slotNumber: Int, button: Int, action: SlotActionType, player: PlayerEntity) {
        if (!TPScreenHandlerUtils.handleGhostSlots(slotNumber, action, button, this, module.items)) {
            super.onSlotClick(slotNumber, button, action, player)
        }
    }

    fun s2cReceiveGateTypeChange(type: ListGateType) {
        gateType.icon = listGateTypeI(type)
        gateType.tooltip = listOf(tooltipLine(listGateTypeT(type)))
    }
}