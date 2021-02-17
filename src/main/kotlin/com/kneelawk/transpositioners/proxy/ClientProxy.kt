package com.kneelawk.transpositioners.proxy

import com.kneelawk.transpositioners.client.screen.TranspositionerScreenUtils

object ClientProxy : CommonProxy {
    override fun presetCursorPosition() {
        TranspositionerScreenUtils.presetCursorPosition()
    }
}