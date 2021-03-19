package com.kneelawk.transpositioners.proxy

interface CommonProxy {
    companion object {
        lateinit var INSTANCE: CommonProxy
    }

    val isClient: Boolean

    fun presetCursorPosition() {}
}