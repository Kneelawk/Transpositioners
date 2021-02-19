package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.TranspositionerScreenHandler
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class TranspositionerScreen(gui: TranspositionerScreenHandler, inv: PlayerInventory, title: Text) :
    CottonInventoryScreen<TranspositionerScreenHandler>(gui, inv.player, title) {

    override fun init(client: MinecraftClient, screenWidth: Int, screenHeight: Int) {
        super.init(client, screenWidth, screenHeight)
        TPScreenUtils.applyCursorPosition()
    }
}
