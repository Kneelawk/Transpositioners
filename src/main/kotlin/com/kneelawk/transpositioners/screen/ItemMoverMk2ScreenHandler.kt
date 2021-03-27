package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.TPConstants.tooltip
import com.kneelawk.transpositioners.client.screen.TPScreenUtils.tooltipLine
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.module.ModuleContext
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.addSlots
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.cycleEnum
import com.kneelawk.transpositioners.util.IconUtils.CHECK_ICON
import com.kneelawk.transpositioners.util.IconUtils.DENY_ICON
import com.kneelawk.transpositioners.util.MovementDirection
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WCardPanel
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Direction
import org.apache.logging.log4j.LogManager
import com.kneelawk.transpositioners.util.IconUtils.extractionSide as extractionSideI
import com.kneelawk.transpositioners.util.IconUtils.insertionSide as insertionSideI
import com.kneelawk.transpositioners.util.IconUtils.movementDirection as movementDirectionI
import com.kneelawk.transpositioners.util.TooltipUtils.extractionSide as extractionSideT
import com.kneelawk.transpositioners.util.TooltipUtils.insertionSide as insertionSideT
import com.kneelawk.transpositioners.util.TooltipUtils.movementDirection as movementDirectionT

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
            NET_PARENT.idSignal("OPEN_PARENT")
        ) { playerInventory.player }
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE")) { playerInventory.player }
        private val ID_DIRECTION_CHANGE = NET_PARENT.idData("DIRECTION_CHANGE", 1)
            .setServerReceiver { module.updateDirection(MovementDirection.byId(it.readByte().toInt())) }
        private val ID_INSERTION_SIDE_CHANGE = NET_PARENT.idData("INSERTION_SIDE_CHANGE", 1)
            .setServerReceiver { module.updateInsertionSide(Direction.byId(it.readByte().toInt())) }
        private val ID_EXTRACTION_SIDE_CHANGE = NET_PARENT.idData("EXTRACTION_SIDE_CHANGE", 1)
            .setServerReceiver { module.updateExtractionSide(Direction.byId(it.readByte().toInt())) }
    }

    private val directionButton: WScalableButton
    private val insertionSideButton: WScalableButton
    private val extractionSideButton: WScalableButton
    private val insertionSideSelector: WBlockSideSelector
    private val extractionSideSelector: WBlockSideSelector

    private var curInsertionSide: Direction
    private var curExtractionSide: Direction

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WCardPanel()
        setRootPanel(root)

        // inventory panel

        val inventoryPanel = WPlainPanel()
        root.add(0, inventoryPanel)

        inventoryPanel.add(createPlayerInventoryPanel(), 0, 3 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        inventoryPanel.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        directionButton = WScalableButton(icon = movementDirectionI(module.direction))
        inventoryPanel.add(directionButton, 2 * 18, 18)
        directionButton.tooltip = listOf(tooltipLine(movementDirectionT(module.direction)))
        directionButton.onClick = {
            val direction = cycleEnum(module.direction)
            ID_DIRECTION_CHANGE.sendToServer(this) { it.writeByte(direction.id) }
        }

        insertionSideButton = WScalableButton(icon = insertionSideI(module.insertionSide))
        inventoryPanel.add(insertionSideButton, 2 * 18, 2 * 18)
        insertionSideButton.tooltip = listOf(tooltipLine(insertionSideT(module.insertionSide)))

        extractionSideButton = WScalableButton(icon = extractionSideI(module.extractionSide))
        inventoryPanel.add(extractionSideButton, 3 * 18, 2 * 18)
        extractionSideButton.tooltip = listOf(tooltipLine(extractionSideT(module.extractionSide)))

        addSlots(inventoryPanel, module.gates, { OPEN_MODULE.send(this, it) }, 0, 2, 5 * 18, 1 * 18)

        // insertion side panel

        val insertionSidePanel = WPlainPanel()
        root.add(1, insertionSidePanel)
        curInsertionSide = module.insertionSide
        insertionSideSelector =
            WBlockSideSelector(
                module.context.world, getInsertionPos(), 5 * 18, 5 * 18, setOf(module.insertionSide)
            )
        insertionSidePanel.add(insertionSideSelector, 2 * 18, 18)
        insertionSideSelector.onSideClicked = { side, _ ->
            curInsertionSide = side
            insertionSideSelector.selectedSide = side
        }
        val insertionSideCancel = WScalableButton(icon = DENY_ICON)
        insertionSidePanel.add(insertionSideCancel, 2 * 18, 6 * 18, 2 * 18 + 9, 18)
        insertionSideCancel.tooltip = listOf(tooltipLine(tooltip("cancel")))
        insertionSideCancel.onClick = {
            curInsertionSide = module.insertionSide
            insertionSideSelector.selectedSide = module.insertionSide
            root.selectedIndex = 0
        }
        val insertionSideOk = WScalableButton(icon = CHECK_ICON)
        insertionSidePanel.add(insertionSideOk, 4 * 18 + 9, 6 * 18, 2 * 18 + 9, 18)
        insertionSideOk.tooltip = listOf(tooltipLine(tooltip("ok")))
        insertionSideOk.onClick = {
            ID_INSERTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(curInsertionSide.id) }
            root.selectedIndex = 0
        }

        // extraction side panel

        val extractionSidePanel = WPlainPanel()
        root.add(2, extractionSidePanel)
        curExtractionSide = module.extractionSide
        extractionSideSelector = WBlockSideSelector(
            module.context.world, getExtractionPos(), 5 * 18, 5 * 18, setOf(module.extractionSide)
        )
        extractionSidePanel.add(extractionSideSelector, 2 * 18, 18)
        extractionSideSelector.onSideClicked = { side, _ ->
            curExtractionSide = side
            extractionSideSelector.selectedSide = side
        }
        val extractionSideCancel = WScalableButton(icon = DENY_ICON)
        extractionSidePanel.add(extractionSideCancel, 2 * 18, 6 * 18, 2 * 18 + 9, 18)
        extractionSideCancel.tooltip = listOf(tooltipLine(tooltip("cancel")))
        extractionSideCancel.onClick = {
            curExtractionSide = module.extractionSide
            extractionSideSelector.selectedSide = module.extractionSide
            root.selectedIndex = 0
        }
        val extractionSideOk = WScalableButton(icon = CHECK_ICON)
        extractionSidePanel.add(extractionSideOk, 4 * 18 + 8, 6 * 18, 2 * 18 + 9, 18)
        extractionSideOk.tooltip = listOf(tooltipLine(tooltip("ok")))
        extractionSideOk.onClick = {
            ID_EXTRACTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(curExtractionSide.id) }
            root.selectedIndex = 0
        }

        // context switch events

        insertionSideButton.onClick = {
            insertionSideSelector.resetRotation()
            root.selectedIndex = 1
        }

        extractionSideButton.onClick = {
            extractionSideSelector.resetRotation()
            root.selectedIndex = 2
        }

        root.validate(this)
    }

    private fun getInsertionPos() = when (module.context) {
        is ModuleContext.Configurator -> module.context.backPos
        is ModuleContext.Entity       -> module.getInsertionPos()
    }

    private fun getExtractionPos() = when (module.context) {
        is ModuleContext.Configurator -> module.context.backPos
        is ModuleContext.Entity       -> module.getExtractionPos()
    }

    override fun close(player: PlayerEntity) {
        insertionSideSelector.close()
        extractionSideSelector.close()
        super.close(player)
    }

    fun s2cReceiveDirectionChange(direction: MovementDirection) {
        directionButton.icon = movementDirectionI(direction)
        directionButton.tooltip = listOf(tooltipLine(movementDirectionT(module.direction)))
        insertionSideSelector.pos = getInsertionPos()
        extractionSideSelector.pos = getExtractionPos()
    }

    fun s2cReceiveInsertionSideChange(side: Direction) {
        insertionSideButton.icon = insertionSideI(side)
        insertionSideButton.tooltip = listOf(tooltipLine(insertionSideT(module.insertionSide)))
        insertionSideSelector.selectedSide = side
    }

    fun s2cReceiveExtractionSideChange(side: Direction) {
        extractionSideButton.icon = extractionSideI(side)
        extractionSideButton.tooltip = listOf(tooltipLine(extractionSideT(module.extractionSide)))
        extractionSideSelector.selectedSide = side
    }
}