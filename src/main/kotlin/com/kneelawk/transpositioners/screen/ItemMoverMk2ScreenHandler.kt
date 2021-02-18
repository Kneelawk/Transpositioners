package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.NetIdSignalK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.module.MovementDirection
import com.kneelawk.transpositioners.proxy.CommonProxy
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager

class ItemMoverMk2ScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    override val module: ItemMoverMk2Module
) :
    SyncedGuiDescription(TranspositionerScreenHandlers.ITEM_MOVER_MK2_TYPE, syncId, playerInventory),
    ModuleScreenHandler {
    companion object {
        val LOGGER = LogManager.getLogger()

        private val NET_PARENT: ParentNetIdSingle<ItemMoverMk2ScreenHandler> = McNetworkStack.SCREEN_HANDLER.subType(
            ItemMoverMk2ScreenHandler::class.java,
            TranspositionersConstants.str("item_mover_mk2_screen_handler")
        )

        private val ID_OPEN_PARENT: NetIdSignalK<ItemMoverMk2ScreenHandler> =
            NET_PARENT.idSignal("OPEN_PARENT").setReceiver(ItemMoverMk2ScreenHandler::receiveOpenParent)
        private val ID_DIRECTION_CHANGE: NetIdDataK<ItemMoverMk2ScreenHandler> =
            NET_PARENT.idData("DIRECTION_CHANGE", 1).setReceiver(ItemMoverMk2ScreenHandler::c2sReceiveDirectionChange)
    }

    private val directionButton: WButton

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WGridPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 3)

        val backButton = WButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.setOnClick { sendOpenParent() }

        root.add(WLabel(TranspositionersConstants.gui("direction")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 1)
        directionButton = WButton(TranspositionersConstants.gui(module.direction.name.toLowerCase()))
        root.add(directionButton, 5, 1, 4, 1)
        directionButton.setOnClick {
            val values = MovementDirection.values()
            val newDirection = values[(module.direction.ordinal + 1) % values.size]
            c2sSendDirectionChange(newDirection)
        }

        root.validate(this)
    }

    private fun sendOpenParent() {
        CommonProxy.INSTANCE.presetCursorPosition()
        ID_OPEN_PARENT.send(CoreMinecraftNetUtil.getClientConnection(), this)
    }

    private fun receiveOpenParent(ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        TranspositionerScreenHandlerUtils.openParentScreen(module, playerInventory.player)
    }

    private fun c2sSendDirectionChange(direction: MovementDirection) {
        ID_DIRECTION_CHANGE.send(CoreMinecraftNetUtil.getClientConnection(), this) { _, buf, ctx ->
            ctx.assertClientSide()
            buf.writeByte(direction.ordinal)
        }
    }

    private fun c2sReceiveDirectionChange(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        val direction = MovementDirection.values()[buf.readByte().toInt()]
        module.updateDirection(direction)
    }

    fun s2cReceiveDirectionChange(direction: MovementDirection) {
        directionButton.label = TranspositionersConstants.gui(direction.name.toLowerCase())
    }
}