package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.item.TranspositionerItems

object TranspositionerModules {
    val MOVERS = ModuleRegistry<MoverModule>()

    fun register() {
        MOVERS.register(ItemMoverMk1Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK1)
        MOVERS.register(ItemMoverMk2Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK2)
        MOVERS.register(ItemMoverMk3Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK3)
    }
}