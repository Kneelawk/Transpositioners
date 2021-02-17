package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemMoverMk2ScreenHandler
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemMoverMk2Screen(gui: ItemMoverMk2ScreenHandler, inv: PlayerInventory, title: Text) :
    CottonInventoryScreen<ItemMoverMk2ScreenHandler>(gui, inv.player, title) {

    override fun init(client: MinecraftClient, screenWidth: Int, screenHeight: Int) {
        super.init(client, screenWidth, screenHeight)
        TranspositionerScreenUtils.applyCursorPosition()
    }
}
