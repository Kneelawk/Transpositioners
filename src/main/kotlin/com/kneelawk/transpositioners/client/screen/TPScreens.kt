package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.TPScreenHandlers
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry

object TPScreens {
    fun register() {
        ScreenRegistry.register(TPScreenHandlers.MODULE_CONFIGURATOR_TYPE, ::ModuleConfiguratorScreen)
        ScreenRegistry.register(TPScreenHandlers.ITEM_GATE_MK1_TYPE, ::ItemGateMk1Screen)
        ScreenRegistry.register(TPScreenHandlers.ITEM_MOVER_MK2_TYPE, ::ItemMoverMk2Screen)
        ScreenRegistry.register(TPScreenHandlers.ITEM_MOVER_MK3_TYPE, ::ItemMoverMk3Screen)
        ScreenRegistry.register(TPScreenHandlers.LOGIC_GATE_TYPE, ::LogicGateScreen)
        ScreenRegistry.register(TPScreenHandlers.NOT_GATE_TYPE, ::NotGateScreen)
        ScreenRegistry.register(TPScreenHandlers.TRANSPOSITIONER_TYPE, ::TranspositionerScreen)
        ScreenRegistry.register(TPScreenHandlers.REDSTONE_GATE_TYPE, ::RedstoneGateScreen)
    }
}