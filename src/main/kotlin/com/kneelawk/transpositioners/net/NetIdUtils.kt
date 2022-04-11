package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun <T> NetIdDataK<T>.sendToServer(t: T, encoder: (NetByteBuf) -> Unit) {
    send(CoreMinecraftNetUtil.getClientConnection(), t) { _, buf, ctx ->
        ctx.assertClientSide()
        encoder(buf)
    }
}

fun <T> NetIdDataK<T>.setServerReceiver(receiver: T.(NetByteBuf) -> Unit): NetIdDataK<T> {
    return setReceiver { obj, buf, ctx ->
        ctx.assertServerSide()
        obj.receiver(buf)
    }
}

fun <T> NetIdDataK<T>.setClientReceiver(receiver: T.(NetByteBuf) -> Unit): NetIdDataK<T> {
    return setReceiver { obj, buf, ctx ->
        ctx.assertClientSide()
        obj.receiver(buf)
    }
}

fun <T> NetIdDataK<T>.sendToWatchingPlayers(world: World, pos: BlockPos, obj: T, write: (NetByteBuf) -> Unit) {
    for (con in CoreMinecraftNetUtil.getPlayersWatching(world, pos)) {
        send(con, obj) { _, buf, ctx ->
            ctx.assertServerSide()
            write(buf)
        }
    }
}
