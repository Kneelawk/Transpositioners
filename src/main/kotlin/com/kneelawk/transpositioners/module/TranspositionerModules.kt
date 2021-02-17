package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.item.TranspositionerItems

object TranspositionerModules {
    val MOVERS = ModuleRegistry<MoverModule>()

    fun register() {
        MOVERS.register(ItemMoverModuleType, TranspositionerItems.ITEM_MOVER_MODULE)
    }
}