package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import com.kneelawk.transpositioners.proxy.CommonProxy
import com.kneelawk.transpositioners.screen.ModuleScreenHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory

class OpenModulePacketHandler<T : ModuleScreenHandler>(
    private val netId: NetIdDataK<T>,
    player: T.() -> PlayerEntity
) {
    init {
        netId.setReceiver { obj, buf, ctx ->
            ctx.assertServerSide()
            val player = obj.player()
            val module = obj.module.getModule(buf.readVarInt()) ?: return@setReceiver

            if (module.context.hasPermission(player)) {
                (module as? NamedScreenHandlerFactory)?.let(player::openHandledScreen)
            }
        }
    }

    fun send(obj: T, index: Int) {
        CommonProxy.INSTANCE.presetCursorPosition()
        netId.send(CoreMinecraftNetUtil.getClientConnection(), obj) { _, buf, ctx ->
            ctx.assertClientSide()
            buf.writeVarInt(index)
        }
    }
}