package com.kneelawk.transpositioners.client

import com.kneelawk.transpositioners.client.entity.TranspositionerEntityRenderer
import com.kneelawk.transpositioners.client.render.TPShaders
import com.kneelawk.transpositioners.client.render.TranspositionerGhostRenderer
import com.kneelawk.transpositioners.client.screen.TPScreens
import com.kneelawk.transpositioners.client.util.TPModels
import com.kneelawk.transpositioners.entity.TPEntityTypes
import com.kneelawk.transpositioners.proxy.ClientProxy
import com.kneelawk.transpositioners.proxy.CommonProxy
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry

fun init() {
    CommonProxy.INSTANCE = ClientProxy

    EntityRendererRegistry.INSTANCE.register(TPEntityTypes.TRANSPOSITIONER) { ctx ->
        TranspositionerEntityRenderer(ctx)
    }

    ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
        for (id in TPModels.TRANSPOSITIONERS) {
            out.accept(id)
        }
        for (id in TPModels.SIDE_SELECTS) {
            out.accept(id)
        }
    }

    TPShaders.register()
    TPScreens.register()
    TranspositionerGhostRenderer.register()
}
