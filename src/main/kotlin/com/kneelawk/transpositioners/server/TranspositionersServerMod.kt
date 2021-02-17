package com.kneelawk.transpositioners.server

import com.kneelawk.transpositioners.proxy.ServerProxy
import com.kneelawk.transpositioners.proxy.CommonProxy

fun init() {
    CommonProxy.INSTANCE = ServerProxy
}
