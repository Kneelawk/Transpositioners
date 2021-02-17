package com.kneelawk.transpositioners.client

import com.kneelawk.transpositioners.client.entity.TranspositionerEntityRenderer
import com.kneelawk.transpositioners.client.screen.TranspositionerScreens
import com.kneelawk.transpositioners.entity.TranspositionerEntityTypes
import com.kneelawk.transpositioners.proxy.ClientProxy
import com.kneelawk.transpositioners.proxy.CommonProxy
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry

fun init() {
    CommonProxy.INSTANCE = ClientProxy

    EntityRendererRegistry.INSTANCE.register(TranspositionerEntityTypes.TRANSPOSITIONER) { dispatcher, _ ->
        TranspositionerEntityRenderer(dispatcher)
    }

    ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
        for (id in TranspositionerEntityRenderer.MODEL_IDS) {
            out.accept(id)
        }
    }

    TranspositionerScreens.register()
}
