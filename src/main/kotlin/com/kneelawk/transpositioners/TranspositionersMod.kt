package com.kneelawk.transpositioners

import com.kneelawk.transpositioners.entity.TranspositionerEntityTypes
import com.kneelawk.transpositioners.item.TranspositionerItems
import com.kneelawk.transpositioners.module.TranspositionerModules
import com.kneelawk.transpositioners.screen.TranspositionerScreenHandlers

fun init() {
    TranspositionerItems.register()
    TranspositionerEntityTypes.register()
    TranspositionerModules.register()
    TranspositionerScreenHandlers.register()
}
