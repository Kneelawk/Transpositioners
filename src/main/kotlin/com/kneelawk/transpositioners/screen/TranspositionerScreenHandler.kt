package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.proxy.CommonProxy
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
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
    TranspositionerScreenHandlers.TRANSPOSITIONER_TYPE,
    syncId,
    playerInventory,
    entity.modules,
    null
) {

    companion object {
        private val LOGGER = LogManager.getLogger()

        private val NET_PARENT: ParentNetIdSingle<TranspositionerScreenHandler> = McNetworkStack.SCREEN_HANDLER.subType(
            TranspositionerScreenHandler::class.java,
            TranspositionersConstants.str("transpositioner_screen_handler")
        )

        private val ID_OPEN_MODULE: NetIdDataK<TranspositionerScreenHandler> =
            NET_PARENT.idData("OPEN_MODULE").setReceiver(TranspositionerScreenHandler::receiveOpenModule)
    }

    init {
        val root = WGridPanel()
        setRootPanel(root)

        val moduleCount = TranspositionerEntity.moduleCountByMk(entity.mk)

        when (entity.mk) {
            1 -> {
                root.add(createPlayerInventoryPanel(), 0, 3)

                val slots = WPlainPanel()
                val slot = WItemSlot.of(entity.modules, 0)
                // buttons are 20 px tall
                slots.add(slot, 54, 1)
                val button = WButton(LiteralText("..."))
                button.isEnabled = entity.modules.getModule(0) is NamedScreenHandlerFactory
                slots.add(button, 90, 0)
                button.setOnClick {
                    sendOpenModule(0)
                }

                slot.addChangeListener { _, _, _, _ ->
                    button.isEnabled = entity.modules.getModule(0) is NamedScreenHandlerFactory
                }

                root.add(slots, 0, 1, 9, 2)
            }
            2 -> {
                root.add(createPlayerInventoryPanel(), 0, 4)

                val slots = WPlainPanel()
                addSlots(slots, 0, moduleCount, 45, 0)

                root.add(slots, 0, 1, 9, 2)
            }
            3 -> {
                root.add(createPlayerInventoryPanel(), 0, 7)

                val slots = WPlainPanel()
                addSlots(slots, 0, moduleCount / 2, 9, 0)
                addSlots(slots, moduleCount / 2, moduleCount / 2, 9, 18 * 3)

                root.add(slots, 0, 1, 9, 2)
            }
            else -> LOGGER.warn("Opened GUI for transpositioner with invalid mk: ${entity.mk}")
        }

        root.validate(this)
    }

    private fun addSlots(
        panel: WPlainPanel,
        startIndex: Int,
        count: Int,
        x: Int,
        y: Int
    ) {
        val slots = WItemSlot.of(entity.modules, startIndex, count, 1)
        panel.add(slots, x, y)
        val buttons = mutableListOf<WButton>()
        for (i in 0 until count) {
            val button = WButton(LiteralText("..."))
            button.isEnabled = entity.modules.getModule(i) is NamedScreenHandlerFactory
            // buttons are 20 px tall
            panel.add(button, x + i * 18, y + 18 + 9 - 1)
            buttons.add(button)
            button.setOnClick {
                sendOpenModule(i)
            }
        }

        slots.addChangeListener { _, _, index, _ ->
            buttons[index - startIndex].isEnabled = entity.modules.getModule(index) is NamedScreenHandlerFactory
        }
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