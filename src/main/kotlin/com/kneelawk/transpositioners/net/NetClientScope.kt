package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.NetIdDataK

@Suppress("UNCHECKED_CAST")
interface NetClientScope<Self : NetClientScope<Self>> {
    fun NetIdDataK<Self>.sendToServer(encoder: (NetByteBuf) -> Unit) {
        sendToServer(this@NetClientScope as Self, encoder)
    }
}