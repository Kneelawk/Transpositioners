package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.proxy.CommonProxy
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.addSlots
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager

class TranspositionerScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    private val entity: TranspositionerEntity
) : SyncedGuiDescription(
    TPScreenHandlers.TRANSPOSITIONER_TYPE,
    syncId,
    playerInventory,
    entity.modules,
    null
) {

    companion object {
        private val LOGGER = LogManager.getLogger()

        private val NET_PARENT: ParentNetIdSingle<TranspositionerScreenHandler> = McNetworkStack.SCREEN_HANDLER.subType(
            TranspositionerScreenHandler::class.java,
            TPConstants.str("transpositioner_screen_handler")
        )

        private val ID_OPEN_MODULE: NetIdDataK<TranspositionerScreenHandler> =
            NET_PARENT.idData("OPEN_MODULE").setReceiver(TranspositionerScreenHandler::receiveOpenModule)
    }

    init {
        val root = WPlainPanel()
        setRootPanel(root)
        root.insets = Insets.ROOT_PANEL

        val moduleCount = TranspositionerEntity.moduleCountByMk(entity.mk)

        when (entity.mk) {
            1 -> {
                root.add(createPlayerInventoryPanel(), 0, 2 * 18 + 9)

                val slots = WPlainPanel()
                val slot = WItemSlot.of(entity.modules, 0)
                slots.add(slot, 3 * 18 + 9, 0)
                val button = WScalableButton(LiteralText("..."))
                button.enabled = entity.modules.getModule(0) is NamedScreenHandlerFactory
                slots.add(button, 4 * 18 + 9, 0)
                button.onClick = {
                    sendOpenModule(0)
                }

                slot.addChangeListener { _, _, _, _ ->
                    button.enabled = entity.modules.getModule(0) is NamedScreenHandlerFactory
                }

                root.add(slots, 0, 18, 9 * 18, 18)
            }
            2 -> {
                root.add(createPlayerInventoryPanel(), 0, 3 * 18 + 9)

                val slots = WPlainPanel()
                addSlots(slots, entity.modules, ::sendOpenModule, 0, moduleCount, 45, 0)

                root.add(slots, 0, 18, 9 * 18, 2 * 18)
            }
            3 -> {
                root.add(createPlayerInventoryPanel(), 0, 6 * 18 + 9)

                val slots = WPlainPanel()
                addSlots(slots, entity.modules, ::sendOpenModule, 0, moduleCount / 2, 9, 0)
                addSlots(slots, entity.modules, ::sendOpenModule, moduleCount / 2, moduleCount / 2, 9, 18 * 3)

                root.add(slots, 0, 18, 9 * 18, 5 * 18)
            }
            else -> LOGGER.warn("Opened GUI for transpositioner with invalid mk: ${entity.mk}")
        }

        root.validate(this)
    }

    private fun sendOpenModule(index: Int) {
        CommonProxy.INSTANCE.presetCursorPosition()
        ID_OPEN_MODULE.send(CoreMinecraftNetUtil.getClientConnection(), this) { _, buf, ctx ->
            ctx.assertClientSide()
            buf.writeVarInt(index)
        }
    }

    private fun receiveOpenModule(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        (entity.modules.getModule(buf.readVarInt()) as? NamedScreenHandlerFactory)?.let {
            playerInventory.player.openHandledScreen(it)
        }
    }
}