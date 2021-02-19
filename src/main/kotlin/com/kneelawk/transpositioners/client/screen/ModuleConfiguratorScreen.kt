package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ModuleConfiguratorScreenHandler
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ModuleConfiguratorScreen(gui: ModuleConfiguratorScreenHandler, inv: PlayerInventory, title: Text) :
    CottonInventoryScreen<ModuleConfiguratorScreenHandler>(gui, inv.player, title) {

    override fun init(client: MinecraftClient?, screenWidth: Int, screenHeight: Int) {
        super.init(client, screenWidth, screenHeight)
        TPScreenUtils.applyCursorPosition()
    }
}