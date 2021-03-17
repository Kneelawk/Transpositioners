package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.addSlots
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.buttonCycleEnum
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.cycleEnum
import com.kneelawk.transpositioners.util.MovementDirection
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
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
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE"), { module.gates },
            { playerInventory.player })
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

    init {
        setTitleAlignment(HorizontalAlignment.RIGHT)

        val root = WPlainPanel()
        setRootPanel(root)

        root.add(createPlayerInventoryPanel(), 0, 3 * 18 + 9)

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        directionButton = WScalableButton(icon = movementDirectionI(module.direction))
        root.add(directionButton, 2 * 18, 18)
        directionButton.tooltip = movementDirectionT(module.direction)
        directionButton.onClick = {
            val direction = cycleEnum(module.direction)
            ID_DIRECTION_CHANGE.sendToServer(this) { it.writeByte(direction.id) }
        }

        insertionSideButton = WScalableButton(icon = insertionSideI(module.insertionSide))
        root.add(insertionSideButton, 2 * 18, 2 * 18)
        insertionSideButton.tooltip = insertionSideT(module.insertionSide)
        insertionSideButton.onClick = { button ->
            val side = buttonCycleEnum(module.insertionSide, button)
            ID_INSERTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(side.id) }
        }

        extractionSideButton = WScalableButton(icon = extractionSideI(module.extractionSide))
        root.add(extractionSideButton, 3 * 18, 2 * 18)
        extractionSideButton.tooltip = extractionSideT(module.extractionSide)
        extractionSideButton.onClick = { button ->
            val side = buttonCycleEnum(module.extractionSide, button)
            ID_EXTRACTION_SIDE_CHANGE.sendToServer(this) { it.writeByte(side.id) }
        }

        addSlots(root, module.gates, { OPEN_MODULE.send(this, it) }, 0, 2, 5 * 18, 1 * 18)

        root.validate(this)
    }

    fun s2cReceiveDirectionChange(direction: MovementDirection) {
        directionButton.icon = movementDirectionI(direction)
        directionButton.tooltip = movementDirectionT(module.direction)
    }

    fun s2cReceiveInsertionSideChange(side: Direction) {
        insertionSideButton.icon = insertionSideI(side)
        insertionSideButton.tooltip = insertionSideT(module.insertionSide)
    }

    fun s2cReceiveExtractionSideChange(side: Direction) {
        extractionSideButton.icon = extractionSideI(side)
        extractionSideButton.tooltip = extractionSideT(module.extractionSide)
    }
}