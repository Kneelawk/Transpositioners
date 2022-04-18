package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.NetIdDataK
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Suppress("UNCHECKED_CAST")
interface NetServerPositionedScope<Self : NetServerPositionedScope<Self>> {
    val world: World
    val pos: BlockPos

    fun NetIdDataK<Self>.sendToWatchingClients(encoder: (NetByteBuf) -> Unit) {
        sendToWatchingClients(world, pos, this@NetServerPositionedScope as Self, encoder)
    }
}