package com.kneelawk.transpositioners.proxy

import com.kneelawk.transpositioners.client.screen.TPScreenUtils

object ClientProxy : CommonProxy {
    override val isClient = true

    override fun presetCursorPosition() {
        TPScreenUtils.presetCursorPosition()
    }
}