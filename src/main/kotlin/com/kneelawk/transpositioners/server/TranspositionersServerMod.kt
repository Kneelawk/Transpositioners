package com.kneelawk.transpositioners.server

import com.kneelawk.transpositioners.proxy.CommonProxy
import com.kneelawk.transpositioners.proxy.ServerProxy

fun init() {
    CommonProxy.INSTANCE = ServerProxy
}
