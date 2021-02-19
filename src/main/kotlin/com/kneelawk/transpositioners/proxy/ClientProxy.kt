package com.kneelawk.transpositioners.proxy

import com.kneelawk.transpositioners.client.screen.TPScreenUtils

object ClientProxy : CommonProxy {
    override fun presetCursorPosition() {
        TPScreenUtils.presetCursorPosition()
    }
}