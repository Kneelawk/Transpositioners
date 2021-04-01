package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdSignalK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.blockentity.ModuleConfiguratorBlockEntity
import com.kneelawk.transpositioners.proxy.CommonProxy
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager

class ModuleConfiguratorScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    private val configurator: ModuleConfiguratorBlockEntity
) : SyncedGuiDescription(
    TPScreenHandlers.MODULE_CONFIGURATOR_TYPE,
    syncId,
    playerInventory,
    configurator.modules,
    null
) {
    companion object {
        private val LOGGER = LogManager.getLogger()

        private val NET_PARENT: ParentNetIdSingle<ModuleConfiguratorScreenHandler> =
            McNetworkStack.SCREEN_HANDLER.subType(
                ModuleConfiguratorScreenHandler::class.java,
                TPConstants.str("module_configurator_screen_handler")
            )

        private val ID_OPEN_MODULE: NetIdSignalK<ModuleConfiguratorScreenHandler> =
            NET_PARENT.idSignal("OPEN_MODULE").setReceiver(ModuleConfiguratorScreenHandler::receiveOpenModule)
    }

    init {
        val root = WPlainPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 2 * 18 + 9)

        val slots = WPlainPanel()
        val slot = WItemSlot.of(configurator.modules, 0)
        slots.add(slot, 3 * 18 + 9, 0)
        val button = WScalableButton(LiteralText("..."))
        button.enabled = configurator.modules.getModule(0) is NamedScreenHandlerFactory
        slots.add(button, 4 * 18 + 9, 0)
        button.onClick = {
            sendOpenModule()
        }

        slot.addChangeListener { _, _, _, _ ->
            button.enabled = configurator.modules.getModule(0) is NamedScreenHandlerFactory
        }

        root.add(slots, 0, 18, 9 * 18, 18)

        root.validate(this)
    }

    private fun sendOpenModule() {
        CommonProxy.INSTANCE.presetCursorPosition()
        ID_OPEN_MODULE.send(CoreMinecraftNetUtil.getClientConnection(), this)
    }

    private fun receiveOpenModule(ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        (configurator.getModule(0) as? NamedScreenHandlerFactory)?.let(playerInventory.player::openHandledScreen)
    }
}