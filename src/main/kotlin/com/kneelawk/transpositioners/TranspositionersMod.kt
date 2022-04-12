package com.kneelawk.transpositioners

import com.kneelawk.transpositioners.block.TPBlocks
import com.kneelawk.transpositioners.blockentity.TPBlockEntities
import com.kneelawk.transpositioners.entity.TPEntityTypes
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.module.TPModules
import com.kneelawk.transpositioners.screen.TPScreenHandlers

fun init() {
    TPBlocks.register()
    TPItems.register()
    TPBlockEntities.register()
    TPEntityTypes.register()
    TPModules.register()
    TPScreenHandlers.register()
    TranspositionerEntity.setupTracking()
}
