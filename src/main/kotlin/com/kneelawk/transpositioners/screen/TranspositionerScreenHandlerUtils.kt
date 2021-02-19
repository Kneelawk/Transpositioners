package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.module.ModuleContext
import com.kneelawk.transpositioners.module.TranspositionerModule
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory

object TranspositionerScreenHandlerUtils {
    fun openParentScreen(module: TranspositionerModule, player: PlayerEntity) {
        player.openHandledScreen(
            (module.path.parent?.findModule(module.context) as? NamedScreenHandlerFactory)
                ?: when (val context = module.context) {
                    is ModuleContext.Configurator -> context.configurator
                    is ModuleContext.Entity -> context.entity
                }
        )
    }

    inline fun <reified E : Enum<E>> cycleEnum(currentValue: E): E {
        val values = E::class.java.enumConstants
        return values[(currentValue.ordinal + 1) % values.size]
    }
}