package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class TranspositionerScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: TranspositionerEntity, context: ScreenHandlerContext) :
    SyncedGuiDescription(TranspositionerScreenHandlers.TRANSPOSITIONER_TYPE, syncId, playerInventory) {

    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(160, 100)

        root.add(createPlayerInventoryPanel(), 0, 3)

        root.validate(this)
    }
}