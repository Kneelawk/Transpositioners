package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.TPScreenHandlers
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry

object TPScreens {
    fun register() {
        ScreenRegistry.register(TPScreenHandlers.MODULE_CONFIGURATOR_TYPE, ::ModuleConfiguratorScreen)
        ScreenRegistry.register(TPScreenHandlers.ITEM_FILTER_MK1_TYPE, ::ItemFilterMk1Screen)
        ScreenRegistry.register(TPScreenHandlers.ITEM_MOVER_MK2_TYPE, ::ItemMoverMk2Screen)
        ScreenRegistry.register(TPScreenHandlers.TRANSPOSITIONER_TYPE, ::TranspositionerScreen)
    }
}