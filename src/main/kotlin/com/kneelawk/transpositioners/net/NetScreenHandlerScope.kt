package com.kneelawk.transpositioners.net

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory

interface NetScreenHandlerScope<Self : NetScreenHandlerScope<Self>> : NetServerPlayerScope<Self>, NetClientScope<Self> {
    val playerInventory: PlayerInventory

    override val player: PlayerEntity
        get() = playerInventory.player
}