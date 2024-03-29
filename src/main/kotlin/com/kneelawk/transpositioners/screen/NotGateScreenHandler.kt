package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.client.screen.TPBackgroundPainters
import com.kneelawk.transpositioners.module.NotGateModule
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.util.IconUtils
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.LiteralText

class NotGateScreenHandler(syncId: Int, playerInventory: PlayerInventory, override val module: NotGateModule) :
    SyncedGuiDescription(TPScreenHandlers.NOT_GATE_TYPE, syncId, playerInventory, module.gates, null),
    ModuleScreenHandler {
    companion object {
        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            NotGateScreenHandler::class.java, str("not_gate_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE")) { playerInventory.player }
    }

    val slot: WItemSlot

    init {
        setTitleAlignment(HorizontalAlignment.CENTER)

        val root = WPlainPanel()
        setRootPanel(root)
        root.insets = Insets.ROOT_PANEL

        root.add(createPlayerInventoryPanel(), 0, 2 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        slot = WItemSlot.of(module.gates, 0)
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

    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        super.addPainters()
        slot.backgroundPainter = TPBackgroundPainters.moduleSlot(IconUtils.GATE_SLOT)
    }
}