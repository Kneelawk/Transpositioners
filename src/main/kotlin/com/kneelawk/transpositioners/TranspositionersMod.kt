package com.kneelawk.transpositioners

import com.kneelawk.transpositioners.entity.TranspositionerEntityTypes
import com.kneelawk.transpositioners.item.TranspositionerItems

fun init() {
    TranspositionerItems.register()
    TranspositionerEntityTypes.register()
}
