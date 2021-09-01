package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil

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
