package com.kneelawk.transpositioners.proxy

interface CommonProxy {
    companion object {
        lateinit var INSTANCE: CommonProxy
    }

    fun presetCursorPosition() {}
}