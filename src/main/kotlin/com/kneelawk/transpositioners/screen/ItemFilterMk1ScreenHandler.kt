package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.module.ItemFilterMk1Module
import com.kneelawk.transpositioners.proxy.CommonProxy
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager

class ItemFilterMk1ScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    override val module: ItemFilterMk1Module
) : SyncedGuiDescription(
    TPScreenHandlers.ITEM_FILTER_MK1_TYPE,
    syncId,
    playerInventory
), ModuleScreenHandler {
    companion object {
        val LOGGER = LogManager.getLogger()

        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            ItemFilterMk1ScreenHandler::class.java,
            str("item_filter_mk1_screen_handler")
        )

        private val ID_OPEN_PARENT =
            NET_PARENT.idSignal("OPEN_PARENT").setReceiver(ItemFilterMk1ScreenHandler::receiveOpenParent)
    }

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WGridPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 3)

        val backButton = WButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.setOnClick { sendOpenParent() }

        root.validate(this)
    }

    private fun sendOpenParent() {
        CommonProxy.INSTANCE.presetCursorPosition()
        ID_OPEN_PARENT.send(CoreMinecraftNetUtil.getClientConnection(), this)
    }

    private fun receiveOpenParent(ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        TPScreenHandlerUtils.openParentScreen(module, playerInventory.player)
    }
}