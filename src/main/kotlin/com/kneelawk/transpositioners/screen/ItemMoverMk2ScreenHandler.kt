package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.NetIdSignalK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TranspositionersConstants.gui
import com.kneelawk.transpositioners.TranspositionersConstants.identifier
import com.kneelawk.transpositioners.TranspositionersConstants.str
import com.kneelawk.transpositioners.item.TranspositionerItems
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.module.MovementDirection
import com.kneelawk.transpositioners.proxy.CommonProxy
import com.kneelawk.transpositioners.screen.TranspositionerScreenHandlerUtils.cycleEnum
import com.kneelawk.transpositioners.screen.TranspositionerScreenHandlerUtils.openParentScreen
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Direction
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
            str("item_mover_mk2_screen_handler")
        )

        private val ID_OPEN_PARENT: NetIdSignalK<ItemMoverMk2ScreenHandler> =
            NET_PARENT.idSignal("OPEN_PARENT").setReceiver(ItemMoverMk2ScreenHandler::receiveOpenParent)
        private val ID_DIRECTION_CHANGE: NetIdDataK<ItemMoverMk2ScreenHandler> =
            NET_PARENT.idData("DIRECTION_CHANGE", 1).setReceiver(ItemMoverMk2ScreenHandler::c2sReceiveDirectionChange)
        private val ID_INSERTION_SIDE_CHANGE = NET_PARENT.idData("INSERTION_SIDE_CHANGE", 1)
            .setReceiver(ItemMoverMk2ScreenHandler::c2sReceiveInsertionSideChange)
        private val ID_EXTRACTION_SIDE_CHANGE = NET_PARENT.idData("EXTRACTION_SIDE_CHANGE", 1)
            .setReceiver(ItemMoverMk2ScreenHandler::c2sReceiveExtractionSideChange)
    }

    private lateinit var directionButton: WButton
    private lateinit var insertionSideButton: WButton
    private lateinit var extractionSideButton: WButton

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WPlainPanel()
        setRootPanel(root)
        root.setSize(13 * 18, 0)

        root.add(createPlayerInventoryPanel(), 2 * 18, 12 * 18)

        val backButton = WButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.setOnClick { sendOpenParent() }

        val tabs = WTabPanel()
        root.add(tabs, 1, 26)

        tabs.add(buildConfigPanel())
        tabs.add(buildFilterPanel())

        root.validate(this)
    }

    private fun buildConfigPanel(): WTabPanel.Tab {
        val buttonPanel = WRectGridPanel(cellHeight = 20)
        buttonPanel.setSize(12 * 18, 7 * 20)

        buttonPanel.add(WLabel(gui("direction")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 0)
        directionButton = WButton(gui(module.direction.name.toLowerCase()))
        buttonPanel.add(directionButton, 7, 0, 5, 1)
        directionButton.setOnClick {
            c2sSendDirectionChange(cycleEnum(module.direction))
        }

        buttonPanel.add(WLabel(gui("insertion_side")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 1)
        insertionSideButton = WButton(gui(module.insertionSide.getName()))
        buttonPanel.add(insertionSideButton, 7, 1, 5, 1)
        insertionSideButton.setOnClick {
            c2sSendInsertionSideChange(cycleEnum(module.insertionSide))
        }

        buttonPanel.add(WLabel(gui("extraction_side")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 2)
        extractionSideButton = WButton(gui(module.extractionSide.getName()))
        buttonPanel.add(extractionSideButton, 7, 2, 5, 1)
        extractionSideButton.setOnClick {
            c2sSendExtractionSideChange(cycleEnum(module.extractionSide))
        }

        val tab = WTabPanel.Tab.Builder(buttonPanel)
        tab.tooltip(gui("tab.config"))
        tab.icon(ItemIcon(TranspositionerItems.TRANSPOSITIONER_CONFIGURATOR))
        return tab.build()
    }

    private fun buildFilterPanel(): WTabPanel.Tab {
        val filterPanel = WRectGridPanel(cellHeight = 20)

        filterPanel.setSize(12 * 18, 7 * 20)

        val tab = WTabPanel.Tab.Builder(filterPanel)
        tab.tooltip(gui("tab.filters"))
        tab.icon(TextureIcon(identifier("textures/gui/filter.png")))
        return tab.build()
    }

    private fun sendOpenParent() {
        CommonProxy.INSTANCE.presetCursorPosition()
        ID_OPEN_PARENT.send(CoreMinecraftNetUtil.getClientConnection(), this)
    }

    private fun receiveOpenParent(ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        openParentScreen(module, playerInventory.player)
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
        directionButton.label = gui(direction.name.toLowerCase())
    }

    private fun c2sSendInsertionSideChange(side: Direction) {
        ID_INSERTION_SIDE_CHANGE.send(CoreMinecraftNetUtil.getClientConnection(), this) { _, buf, ctx ->
            ctx.assertClientSide()
            buf.writeByte(side.id)
        }
    }

    private fun c2sReceiveInsertionSideChange(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        val side = Direction.byId(buf.readByte().toInt())
        module.updateInsertionSide(side)
    }

    fun s2cReceiveInsertionSideChange(side: Direction) {
        insertionSideButton.label = gui(side.getName())
    }

    private fun c2sSendExtractionSideChange(side: Direction) {
        ID_EXTRACTION_SIDE_CHANGE.send(CoreMinecraftNetUtil.getClientConnection(), this) { _, buf, ctx ->
            ctx.assertClientSide()
            buf.writeByte(side.id)
        }
    }

    private fun c2sReceiveExtractionSideChange(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertServerSide()
        val side = Direction.byId(buf.readByte().toInt())
        module.updateExtractionSide(side)
    }

    fun s2cReceiveExtractionSideChange(side: Direction) {
        extractionSideButton.label = gui(side.getName())
    }
}