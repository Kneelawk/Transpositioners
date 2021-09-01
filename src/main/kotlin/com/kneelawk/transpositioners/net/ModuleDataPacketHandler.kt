package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import com.kneelawk.transpositioners.module.Module
import com.kneelawk.transpositioners.screen.ModuleScreenHandler
import kotlin.reflect.KClass

/**
 * Designed to simplify the process of a server-side module updating its client-side counterpart and client-side screen.
 */
class ModuleDataPacketHandler<M : Module, S : ModuleScreenHandler>(
    private val netId: NetIdDataK<M>, screenHandlerClass: KClass<S>, stateEncoder: M.(NetByteBuf) -> Unit,
    stateDecoder: M.(NetByteBuf) -> Unit, screenNotifier: M.(S) -> Unit
) {

    init {
        netId.setReadWrite({ module, buf, ctx ->
            ctx.assertClientSide()
            module.stateDecoder(buf)
            val handler = ctx.connection.player.currentScreenHandler
            if (screenHandlerClass.isInstance(handler)) {
                @Suppress("UNCHECKED_CAST")
                handler as S
                if (handler.module === module) {
                    module.screenNotifier(handler)
                }
            }
        }, { module, buf, ctx ->
            ctx.assertServerSide()
            module.stateEncoder(buf)
        })
    }

    fun sendToClients(module: M) {
        // Using frontPos because that's the actual location of the transpositioner.
        for (con in CoreMinecraftNetUtil.getPlayersWatching(module.context.world, module.context.frontPos)) {
            netId.send(con, module)
        }
    }
}