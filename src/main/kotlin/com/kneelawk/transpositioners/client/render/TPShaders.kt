package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.identifier
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.render.Shader
import net.minecraft.client.render.VertexFormats
import net.minecraft.resource.ResourceFactory
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object TPShaders : SimpleSynchronousResourceReloadListener {
    private val NAMESPACES =
        mapOf("shaders/include/light.glsl" to "minecraft", "shaders/include/fog.glsl" to "minecraft")

    lateinit var GHOST: Shader
        private set

    lateinit var PLACEMENT: Shader
        private set

    fun register() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this)
    }

    override fun getFabricId(): Identifier {
        return identifier("shaders")
    }

    override fun reload(manager: ResourceManager) {
        GHOST = Shader(
            wrap(manager),
            "transpositioner_ghost",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        )

        PLACEMENT = Shader(
            wrap(manager),
            "transpositioner_placement",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        )
    }

    private fun wrap(factory: ResourceFactory): ResourceFactory {
        return ResourceFactory { id ->
            factory.getResource(Identifier(NAMESPACES.getOrDefault(id.path, TPConstants.MOD_ID), id.path))
        }
    }
}

data class ShaderSet(val ghost: Shader)
