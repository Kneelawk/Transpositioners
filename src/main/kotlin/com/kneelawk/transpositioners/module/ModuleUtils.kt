package com.kneelawk.transpositioners.module

import alexiil.mc.lib.net.IMsgReadCtx
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.screen.ModuleScreenHandler
import com.kneelawk.transpositioners.util.ListGateType
import com.kneelawk.transpositioners.util.MovementDirection
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Direction

object ModuleUtils {
    inline fun <reified S : ModuleScreenHandler> screenHandler(
        ctx: IMsgReadCtx,
        module: Module,
        handlerFn: (S) -> Unit
    ) {
        (ctx.connection.player.currentScreenHandler as? S)?.let { handler ->
            if (handler.module === module) {
                handlerFn(handler)
            }
        }
    }
}