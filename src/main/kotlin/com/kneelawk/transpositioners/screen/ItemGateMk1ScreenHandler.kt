package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.identifier
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.client.screen.icon.EnhancedTextureIcon
import com.kneelawk.transpositioners.module.ItemGateMk1Module
import com.kneelawk.transpositioners.module.ModuleUtils
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.cycleEnum
import com.kneelawk.transpositioners.util.ListGateType
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager

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

        private val CHECK_ICON = EnhancedTextureIcon(identifier("textures/gui/check2.png"), 16, 16)
        private val DENY_ICON = EnhancedTextureIcon(identifier("textures/gui/deny2.png"), 16, 16)

        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            ItemGateMk1ScreenHandler::class.java,
            str("item_gate_mk1_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val ID_GATE_TYPE_GHANGE = NET_PARENT.idData("GATE_TYPE_CHANGE")
            .setServerReceiver { module.updateGateType(ListGateType.byId(it.readByte().toInt())) }
    }

    private val gateType: WScalableButton

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WGridPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 4)

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.setOnClick { OPEN_PARENT.send(this) }

        val slots = WItemSlot.of(module.items, 0, 3, 3)
        root.add(slots, 3, 1)

        gateType = WScalableButton(
            icon = when (module.gateType) {
                ListGateType.ALLOW -> CHECK_ICON
                ListGateType.DENY  -> DENY_ICON
            }
        ).setTooltip(TPConstants.tooltip("list_gate_type", ModuleUtils.listGateTypeTooltip(module.gateType)))
        root.add(gateType, 7, 2)
        gateType.setOnClick {
            val gateType = cycleEnum(module.gateType)
            ID_GATE_TYPE_GHANGE.sendToServer(this) { it.writeByte(gateType.id) }
        }

        root.validate(this)
    }

    override fun onSlotClick(slotNumber: Int, button: Int, action: SlotActionType, player: PlayerEntity): ItemStack {
        return TPScreenHandlerUtils.handleGhostSlots(slotNumber, player, action, button, this, module.items)
            ?: super.onSlotClick(slotNumber, button, action, player)
    }

    fun s2cReceiveGateTypeChange(type: ListGateType) {
        gateType.setIcon(
            when (type) {
                ListGateType.ALLOW -> CHECK_ICON
                ListGateType.DENY  -> DENY_ICON
            }
        ).setTooltip(TPConstants.tooltip("list_gate_type", ModuleUtils.listGateTypeTooltip(type)))
    }
}