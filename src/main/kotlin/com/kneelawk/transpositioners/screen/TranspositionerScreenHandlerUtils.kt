package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.module.TranspositionerModule
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory

object TranspositionerScreenHandlerUtils {
    fun openParentScreen(module: TranspositionerModule, player: PlayerEntity) {
        player.openHandledScreen(
            (module.path.parent?.findModule(module.entity) as? NamedScreenHandlerFactory) ?: module.entity
        )
    }
}