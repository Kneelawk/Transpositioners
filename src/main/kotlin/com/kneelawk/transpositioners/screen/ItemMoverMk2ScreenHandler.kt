package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.gui
import com.kneelawk.transpositioners.TPConstants.identifier
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.module.MovementDirection
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.addSlots
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.cycleEnum
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
import net.minecraft.item.Items
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Direction
import org.apache.logging.log4j.LogManager

class ItemMoverMk2ScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    override val module: ItemMoverMk2Module
) :
    SyncedGuiDescription(TPScreenHandlers.ITEM_MOVER_MK2_TYPE, syncId, playerInventory),
    ModuleScreenHandler {
    companion object {
        val LOGGER = LogManager.getLogger()

        private val NET_PARENT: ParentNetIdSingle<ItemMoverMk2ScreenHandler> = McNetworkStack.SCREEN_HANDLER.subType(
            ItemMoverMk2ScreenHandler::class.java,
            str("item_mover_mk2_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(
            NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE"), { module.gates },
            { playerInventory.player })
        private val ID_DIRECTION_CHANGE = NET_PARENT.idData("DIRECTION_CHANGE", 1)
            .setServerReceiver { module.updateDirection(MovementDirection.byId(it.readByte().toInt())) }
        private val ID_INSERTION_SIDE_CHANGE = NET_PARENT.idData("INSERTION_SIDE_CHANGE", 1)
            .setServerReceiver { module.updateInsertionSide(Direction.byId(it.readByte().toInt())) }
        private val ID_EXTRACTION_SIDE_CHANGE = NET_PARENT.idData("EXTRACTION_SIDE_CHANGE", 1)
            .setServerReceiver { module.updateExtractionSide(Direction.byId(it.readByte().toInt())) }
    }

    private lateinit var directionButton: WButton
    private lateinit var insertionSideButton: WButton
    private lateinit var extractionSideButton: WButton

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WPlainPanel()
        setRootPanel(root)
        root.setSize(13 * 18, 0)

        root.add(createPlayerInventoryPanel(), 2 * 18, 8 * 18)

        val backButton = WButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.setOnClick { OPEN_PARENT.send(this) }

        val tabs = WTabPanel()
        root.add(tabs, 1, 26)

        tabs.add(buildConfigPanel())
        tabs.add(buildGatePanel())

        root.validate(this)
    }

    private fun buildConfigPanel(): WTabPanel.Tab {
        val buttonPanel = WRectGridPanel(cellHeight = 20)
        buttonPanel.setSize(12 * 18, 3 * 20)

        buttonPanel.add(WLabel(gui("direction")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 0)
        directionButton = WButton(gui(module.direction.name.toLowerCase()))
        buttonPanel.add(directionButton, 7, 0, 5, 1)
        directionButton.setOnClick {
            val direction = cycleEnum(module.direction)
            ID_DIRECTION_CHANGE.sendToServer(this) { it.writeByte(direction.id) }
        }

        buttonPanel.add(WLabel(gui("insertion_side")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 1)
        insertionSideButton = WButton(gui(module.insertionSide.getName()))
        buttonPanel.add(insertionSideButton, 7, 1, 5, 1)
        insertionSideButton.setOnClick {
            val side = cycleEnum(module.insertionSide)
            ID_INSERTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(side.id) }
        }

        buttonPanel.add(WLabel(gui("extraction_side")).apply {
            verticalAlignment = VerticalAlignment.CENTER
        }, 0, 2)
        extractionSideButton = WButton(gui(module.extractionSide.getName()))
        buttonPanel.add(extractionSideButton, 7, 2, 5, 1)
        extractionSideButton.setOnClick {
            val side = cycleEnum(module.extractionSide)
            ID_EXTRACTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(side.id) }
        }

        val tab = WTabPanel.Tab.Builder(buttonPanel)
        tab.tooltip(gui("tab.config"))
        tab.icon(ItemIcon(TPItems.TRANSPOSITIONER_CONFIGURATOR))
        return tab.build()
    }

    private fun buildGatePanel(): WTabPanel.Tab {
        val gatePanel = WPlainPanel()
        gatePanel.setSize(12 * 18, 3 * 20)

        addSlots(gatePanel, module.gates, { OPEN_MODULE.send(this, it) }, 0, 2, 5 * 18, 0)

        val tab = WTabPanel.Tab.Builder(gatePanel)
        tab.tooltip(gui("tab.gates"))
        tab.icon(ItemIcon(Items.OAK_FENCE_GATE))
        return tab.build()
    }

    fun s2cReceiveDirectionChange(direction: MovementDirection) {
        directionButton.label = gui(direction.name.toLowerCase())
    }

    fun s2cReceiveInsertionSideChange(side: Direction) {
        insertionSideButton.label = gui(side.getName())
    }

    fun s2cReceiveExtractionSideChange(side: Direction) {
        extractionSideButton.label = gui(side.getName())
    }
}