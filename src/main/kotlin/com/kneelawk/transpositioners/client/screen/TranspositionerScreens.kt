package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.TranspositionerScreenHandlers
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry

object TranspositionerScreens {
    fun register() {
        ScreenRegistry.register(TranspositionerScreenHandlers.TRANSPOSITIONER_TYPE, ::TranspositionerScreen)
    }
}