package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import com.kneelawk.transpositioners.module.ModuleInventory
import com.kneelawk.transpositioners.proxy.CommonProxy
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory

class OpenModulePacketHandler<T>(private val netId: NetIdDataK<T>, moduleInventory: T.() -> ModuleInventory<*>,
                                 player: T.() -> PlayerEntity) {
    init {
        netId.setReceiver { obj, buf, ctx ->
            ctx.assertServerSide()
            (obj.moduleInventory().getModule(buf.readVarInt()) as? NamedScreenHandlerFactory)?.let(
                obj.player()::openHandledScreen)
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