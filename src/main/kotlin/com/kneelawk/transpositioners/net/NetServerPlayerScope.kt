package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.NetIdDataK
import net.minecraft.entity.player.PlayerEntity

@Suppress("UNCHECKED_CAST")
interface NetServerPlayerScope<Self : NetServerPlayerScope<Self>> {
    val player: PlayerEntity

    fun NetIdDataK<Self>.sendToClient(encoder: (NetByteBuf) -> Unit) {
        sendToClient(this@NetServerPlayerScope as Self, player, encoder)
    }
}