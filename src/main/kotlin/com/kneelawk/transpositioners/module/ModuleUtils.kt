package com.kneelawk.transpositioners.module

import alexiil.mc.lib.net.IMsgReadCtx
import com.kneelawk.transpositioners.screen.ModuleScreenHandler

object ModuleUtils {
    inline fun <reified S : ModuleScreenHandler> screenHandler(
        ctx: IMsgReadCtx,
        module: TranspositionerModule,
        handlerFn: (S) -> Unit
    ) {
        (ctx.connection.player.currentScreenHandler as? S)?.let { handler ->
            if (handler.module === module) {
                handlerFn(handler)
            }
        }
    }
}