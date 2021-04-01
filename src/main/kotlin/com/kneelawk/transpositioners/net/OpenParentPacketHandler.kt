package com.kneelawk.transpositioners.net

import alexiil.mc.lib.net.NetIdSignalK
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import com.kneelawk.transpositioners.proxy.CommonProxy
import com.kneelawk.transpositioners.screen.ModuleScreenHandler
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils
import net.minecraft.entity.player.PlayerEntity

class OpenParentPacketHandler<T : ModuleScreenHandler>(private val netId: NetIdSignalK<T>,
                                                       player: T.() -> PlayerEntity) {
    init {
        netId.setReceiver { obj, ctx ->
            ctx.assertServerSide()
            TPScreenHandlerUtils.openParentScreen(obj.module, obj.player())
        }
    }

    fun send(obj: T) {
        CommonProxy.INSTANCE.presetCursorPosition()
        netId.send(CoreMinecraftNetUtil.getClientConnection(), obj)
    }
}