package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.module.ItemNotGateModule
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.LiteralText

class ItemNotGateScreenHandler(syncId: Int, playerInventory: PlayerInventory, override val module: ItemNotGateModule) :
    SyncedGuiDescription(TPScreenHandlers.ITEM_NOT_GATE_TYPE, syncId, playerInventory, module.gates, null),
    ModuleScreenHandler {
    companion object {
        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            ItemNotGateScreenHandler::class.java, str("item_not_gate_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE")) { playerInventory.player }
    }

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WPlainPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 2 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        val slot = WItemSlot.of(module.gates, 0)
        root.add(slot, 3 * 18 + 9, 18)
        val button = WScalableButton(LiteralText("..."))
        button.enabled = module.gates.getModule(0) is NamedScreenHandlerFactory
        root.add(button, 4 * 18 + 9, 18)
        button.onClick = {
            OPEN_MODULE.send(this, 0)
        }

        slot.addChangeListener { _, _, _, _ ->
            button.enabled = module.gates.getModule(0) is NamedScreenHandlerFactory
        }

        root.validate(this)
    }
}