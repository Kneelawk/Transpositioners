package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager

class TranspositionerScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    entity: TranspositionerEntity,
    context: ScreenHandlerContext
) :
    SyncedGuiDescription(
        TranspositionerScreenHandlers.TRANSPOSITIONER_TYPE,
        syncId,
        playerInventory,
        entity.modules,
        null
    ) {

    companion object {
        private val LOGGER = LogManager.getLogger()
    }

    init {
        val root = WGridPanel()
        setRootPanel(root)
        // this seems to get resized when it gets validated
        root.setSize(160, 100)

        val moduleCount = TranspositionerEntity.moduleCountByMk(entity.mk)

        when (entity.mk) {
            1 -> {
                root.add(createPlayerInventoryPanel(), 0, 3)

                val slots = WPlainPanel()
                // buttons are 20 px tall
                slots.add(WItemSlot.of(entity.modules, 0), 54, 1)
                val button = WButton(LiteralText("..."))
                button.isEnabled = false
                slots.add(button, 90, 0)

                root.add(slots, 0, 1, 9, 2)
            }
            2 -> {
                root.add(createPlayerInventoryPanel(), 0, 4)

                val slots = WPlainPanel()
                addSlots(slots, entity, 0, moduleCount, 45, 0)

                root.add(slots, 0, 1, 9, 2)
            }
            3 -> {
                root.add(createPlayerInventoryPanel(), 0, 7)

                val slots = WPlainPanel()
                addSlots(slots, entity, 0, moduleCount / 2, 9, 0)
                addSlots(slots, entity, moduleCount / 2, moduleCount / 2, 9, 18 * 3)

                root.add(slots, 0, 1, 9, 2)
            }
            else -> LOGGER.warn("Opened GUI for transpositioner with invalid mk: ${entity.mk}")
        }

        root.validate(this)
    }

    private fun addSlots(
        panel: WPlainPanel,
        entity: TranspositionerEntity,
        startIndex: Int,
        count: Int,
        x: Int,
        y: Int
    ) {
        panel.add(WItemSlot.of(entity.modules, startIndex, count, 1), x, y)
        for (i in 0 until count) {
            val button = WButton(LiteralText("..."))
            button.isEnabled = false
            // buttons are 20 px tall
            panel.add(button, x + i * 18, y + 18 + 9 - 1)
        }
    }
}