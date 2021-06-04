package com.kneelawk.transpositioners.client.screen

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

abstract class AbstractTPScreen<T : SyncedGuiDescription>(gui: T, player: PlayerEntity, title: Text) :
    CottonInventoryScreen<T>(gui, player, title) {

    override fun init() {
        super.init()
        TPScreenUtils.applyCursorPosition()
    }
}