package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.TPConstants.tooltip
import com.kneelawk.transpositioners.client.screen.TPBackgroundPainters
import com.kneelawk.transpositioners.client.screen.TPScreenUtils
import com.kneelawk.transpositioners.module.ItemMoverMk3Module
import com.kneelawk.transpositioners.module.ModuleContext
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.util.IconUtils
import com.kneelawk.transpositioners.util.MovementDirection
import com.kneelawk.transpositioners.util.TooltipUtils
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WCardPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Direction

class ItemMoverMk3ScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, override val module: ItemMoverMk3Module
) : SyncedGuiDescription(TPScreenHandlers.ITEM_MOVER_MK3_TYPE, syncId, playerInventory, module.gates, null),
    ModuleScreenHandler {
    companion object {
        private val NET_PARENT: ParentNetIdSingle<ItemMoverMk3ScreenHandler> = McNetworkStack.SCREEN_HANDLER.subType(
            ItemMoverMk3ScreenHandler::class.java, str("item_mover_mk3_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE")) { playerInventory.player }
        private val ID_DIRECTION_CHANGE = NET_PARENT.idData("DIRECTION_CHANGE", 1)
            .setServerReceiver { module.updateDirection(MovementDirection.byId(it.readByte().toInt())) }
        private val ID_INSERTION_SIDE_CHANGE = NET_PARENT.idData("INSERTION_SIDE_CHANGE", 1)
            .setServerReceiver { module.updateInsertionSide(Direction.byId(it.readByte().toInt())) }
        private val ID_EXTRACTION_SIDE_CHANGE = NET_PARENT.idData("EXTRACTION_SIDE_CHANGE", 1)
            .setServerReceiver { module.updateExtractionSide(Direction.byId(it.readByte().toInt())) }
        private val ID_STACK_SIZE_CHANGE = NET_PARENT.idData("STACK_SIZE_CHANGE")
            .setServerReceiver { module.updateStackSize(it.readVarUnsignedInt()) }
        private val ID_TICKS_PER_MOVE_CHANGE = NET_PARENT.idData("TICKS_PER_MOVE_CHANGE")
            .setServerReceiver { module.updateTicksPerMove(it.readVarUnsignedInt()) }
    }

    private val directionButton: WScalableButton
    private val insertionSideButton: WScalableButton
    private val extractionSideButton: WScalableButton
    private val insertionSideSelector: WBlockSideSelector
    private val extractionSideSelector: WBlockSideSelector
    private val stackSizeField: WSpecialTextField
    private val ticksPerMoveField: WSpecialTextField
    private val gateSlots: WItemSlot

    private var curInsertionSide: Direction
    private var curExtractionSide: Direction
    private var curStackSize = module.stackSize
    private var curTicksPerMove = module.ticksPerMove

    init {
        setTitleAlignment(HorizontalAlignment.CENTER)

        val root = WCardPanel()
        setRootPanel(root)

        // inventory panel

        val inventoryPanel = WPlainPanel()
        inventoryPanel.insets = Insets.ROOT_PANEL

        inventoryPanel.add(createPlayerInventoryPanel(), 0, 4 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        inventoryPanel.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        // Text fields first because they're a little messy and draw over things
        stackSizeField = WSpecialTextField()
        inventoryPanel.add(stackSizeField, 18, 3 * 18, 2 * 18, 18)
        stackSizeField.setText(module.stackSize.toString())
        stackSizeField.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("field.stack_size")))
        stackSizeField.textFilter = TPScreenHandlerUtils::integerStringFilter

        ticksPerMoveField = WSpecialTextField()
        inventoryPanel.add(ticksPerMoveField, 5 * 18, 3 * 18, 2 * 18, 18)
        ticksPerMoveField.setText(module.ticksPerMove.toString())
        ticksPerMoveField.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("field.ticks_per_move")))
        ticksPerMoveField.textFilter = TPScreenHandlerUtils::integerStringFilter

        directionButton = WScalableButton(icon = IconUtils.movementDirection(module.direction))
        inventoryPanel.add(directionButton, 2 * 18, 18, 2 * 18, 18)
        directionButton.tooltip = listOf(TPScreenUtils.tooltipLine(TooltipUtils.movementDirection(module.direction)))
        directionButton.onClick = {
            val direction = TPScreenHandlerUtils.cycleEnum(module.direction)
            ID_DIRECTION_CHANGE.sendToServer(this) { it.writeByte(direction.id) }
        }

        insertionSideButton = WScalableButton(icon = IconUtils.insertionSide(module.insertionSide))
        inventoryPanel.add(insertionSideButton, 3 * 18, 2 * 18)
        insertionSideButton.tooltip = listOf(
            TPScreenUtils.tooltipLine(TooltipUtils.insertionSide(module.insertionSide))
        )

        extractionSideButton = WScalableButton(icon = IconUtils.extractionSide(module.extractionSide))
        inventoryPanel.add(extractionSideButton, 2 * 18, 2 * 18)
        extractionSideButton.tooltip = listOf(
            TPScreenUtils.tooltipLine(TooltipUtils.extractionSide(module.extractionSide))
        )

        gateSlots = TPScreenHandlerUtils.addSlots(
            inventoryPanel, module.gates, { OPEN_MODULE.send(this, it) }, 0, 1, 5 * 18, 1 * 18
        )

        val stackSizeOkButton = WScalableButton(icon = IconUtils.CHECK_SMALL_ICON)
        inventoryPanel.add(stackSizeOkButton, 3 * 18, 3 * 18, 18, 9)
        stackSizeOkButton.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("ok")))
        stackSizeOkButton.onClick = {
            try {
                curStackSize = Integer.parseInt(stackSizeField.text)
            } catch (e: NumberFormatException) {
                stackSizeField.setText(curStackSize.toString())
            }
            stackSizeField.releaseFocus()
            ID_STACK_SIZE_CHANGE.sendToServer(this) { it.writeVarUnsignedInt(curStackSize) }
        }

        val stackSizeCancelButton = WScalableButton(icon = IconUtils.DENY_SMALL_ICON)
        inventoryPanel.add(stackSizeCancelButton, 3 * 18, 3 * 18 + 9, 18, 9)
        stackSizeCancelButton.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("cancel")))
        stackSizeCancelButton.onClick = {
            stackSizeField.setText(curStackSize.toString())
            stackSizeField.releaseFocus()
        }

        val ticksPerMoveOkButton = WScalableButton(icon = IconUtils.CHECK_SMALL_ICON)
        inventoryPanel.add(ticksPerMoveOkButton, 7 * 18, 3 * 18, 18, 9)
        ticksPerMoveOkButton.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("ok")))
        ticksPerMoveOkButton.onClick = {
            try {
                curTicksPerMove = Integer.parseInt(ticksPerMoveField.text)
            } catch (e: NumberFormatException) {
                ticksPerMoveField.setText(curTicksPerMove.toString())
            }
            ticksPerMoveField.releaseFocus()
            ID_TICKS_PER_MOVE_CHANGE.sendToServer(this) { it.writeVarUnsignedInt(curTicksPerMove) }
        }

        val ticksPerMoveCancelButton = WScalableButton(icon = IconUtils.DENY_SMALL_ICON)
        inventoryPanel.add(ticksPerMoveCancelButton, 7 * 18, 3 * 18 + 9, 18, 9)
        ticksPerMoveCancelButton.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("cancel")))
        ticksPerMoveCancelButton.onClick = {
            ticksPerMoveField.setText(curTicksPerMove.toString())
            ticksPerMoveField.releaseFocus()
        }

        root.add(0, inventoryPanel)

        // insertion side panel

        val insertionSidePanel = WPlainPanel()
        insertionSidePanel.insets = Insets.ROOT_PANEL
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
        val insertionSideCancel = WScalableButton(icon = IconUtils.DENY_ICON)
        insertionSidePanel.add(insertionSideCancel, 2 * 18, 6 * 18, 2 * 18 + 9, 18)
        insertionSideCancel.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("cancel")))
        insertionSideCancel.onClick = {
            curInsertionSide = module.insertionSide
            insertionSideSelector.selectedSide = module.insertionSide
            root.selectedIndex = 0
        }
        val insertionSideOk = WScalableButton(icon = IconUtils.CHECK_ICON)
        insertionSidePanel.add(insertionSideOk, 4 * 18 + 9, 6 * 18, 2 * 18 + 9, 18)
        insertionSideOk.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("ok")))
        insertionSideOk.onClick = {
            ID_INSERTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(curInsertionSide.id) }
            root.selectedIndex = 0
        }
        root.add(1, insertionSidePanel)

        // extraction side panel

        val extractionSidePanel = WPlainPanel()
        extractionSidePanel.insets = Insets.ROOT_PANEL
        curExtractionSide = module.extractionSide
        extractionSideSelector = WBlockSideSelector(
            module.context.world, getExtractionPos(), 5 * 18, 5 * 18, setOf(module.extractionSide)
        )
        extractionSidePanel.add(extractionSideSelector, 2 * 18, 18)
        extractionSideSelector.onSideClicked = { side, _ ->
            curExtractionSide = side
            extractionSideSelector.selectedSide = side
        }
        val extractionSideCancel = WScalableButton(icon = IconUtils.DENY_ICON)
        extractionSidePanel.add(extractionSideCancel, 2 * 18, 6 * 18, 2 * 18 + 9, 18)
        extractionSideCancel.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("cancel")))
        extractionSideCancel.onClick = {
            curExtractionSide = module.extractionSide
            extractionSideSelector.selectedSide = module.extractionSide
            root.selectedIndex = 0
        }
        val extractionSideOk = WScalableButton(icon = IconUtils.CHECK_ICON)
        extractionSidePanel.add(extractionSideOk, 4 * 18 + 8, 6 * 18, 2 * 18 + 9, 18)
        extractionSideOk.tooltip = listOf(TPScreenUtils.tooltipLine(tooltip("ok")))
        extractionSideOk.onClick = {
            ID_EXTRACTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(curExtractionSide.id) }
            root.selectedIndex = 0
        }
        root.add(2, extractionSidePanel)

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
        is ModuleContext.Entity -> module.getInsertionPos()
    }

    private fun getExtractionPos() = when (module.context) {
        is ModuleContext.Configurator -> module.context.backPos
        is ModuleContext.Entity -> module.getExtractionPos()
    }

    override fun close(player: PlayerEntity) {
        insertionSideSelector.close()
        extractionSideSelector.close()
        super.close(player)
    }

    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        super.addPainters()
        gateSlots.backgroundPainter = TPBackgroundPainters.moduleSlot(IconUtils.ITEM_GATE_SLOT)
    }

    fun s2cReceiveDirectionChange(direction: MovementDirection) {
        directionButton.icon = IconUtils.movementDirection(direction)
        directionButton.tooltip = listOf(TPScreenUtils.tooltipLine(TooltipUtils.movementDirection(module.direction)))
        insertionSideSelector.pos = getInsertionPos()
        extractionSideSelector.pos = getExtractionPos()
    }

    fun s2cReceiveInsertionSideChange(side: Direction) {
        insertionSideButton.icon = IconUtils.insertionSide(side)
        insertionSideButton.tooltip = listOf(
            TPScreenUtils.tooltipLine(TooltipUtils.insertionSide(module.insertionSide))
        )
        insertionSideSelector.selectedSide = side
    }

    fun s2cReceiveExtractionSideChange(side: Direction) {
        extractionSideButton.icon = IconUtils.extractionSide(side)
        extractionSideButton.tooltip = listOf(
            TPScreenUtils.tooltipLine(TooltipUtils.extractionSide(module.extractionSide))
        )
        extractionSideSelector.selectedSide = side
    }

    fun s2cReceiveStackSizeChange(stackSize: Int) {
        stackSizeField.setText(stackSize.toString())
    }

    fun s2cReceiveMovesPerSecondChange(ticksPerMove: Int) {
        ticksPerMoveField.setText(ticksPerMove.toString())
    }
}