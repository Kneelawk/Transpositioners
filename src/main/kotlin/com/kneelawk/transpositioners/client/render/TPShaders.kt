package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.TPConstants.identifier
import com.kneelawk.transpositioners.TPConstants.str
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Shader
import net.minecraft.client.render.VertexFormats
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.math.Matrix4f

object TPShaders {
    private val PLACEMENT_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)
    private val GHOST_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)

    lateinit var TRANSPOSITIONER_GHOST: Shader
    lateinit var TRANSPOSITIONER_PLACEMENT: Shader
    lateinit var SIDE_SELECTOR_INDICATOR: Shader
    lateinit var SIDE_SELECTOR_HOVER: Shader

    fun register() {
        WorldRenderEvents.START.register { ctx ->
            update(ctx.tickDelta())
        }
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun reload(manager: ResourceManager) {
                    TRANSPOSITIONER_GHOST = Shader(
                        manager, str("transpositioner_ghost"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
                    )
                    TRANSPOSITIONER_PLACEMENT = Shader(
                        manager, str("transpositioner_placement"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
                    )
                    SIDE_SELECTOR_INDICATOR = Shader(
                        manager, str("side_selector_indicator"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
                    )
                    SIDE_SELECTOR_HOVER = Shader(
                        manager, str("side_selector_hover"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
                    )
                }

                override fun getFabricId(): Identifier = identifier("shader_reload")
            })
    }

    private fun update(tickDelta: Float) {
        var placementDelta = 0f

        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            val world = player.world
            placementDelta = world.time + tickDelta
        }

        // Setup shader uniforms
        TRANSPOSITIONER_GHOST.getUniform("DepthRangeMat")?.set(GHOST_DEPTH_RANGE)
        TRANSPOSITIONER_PLACEMENT.getUniform("PlacementDelta")?.set(placementDelta)
        TRANSPOSITIONER_PLACEMENT.getUniform("DepthRangeMat")?.set(PLACEMENT_DEPTH_RANGE)
        SIDE_SELECTOR_HOVER.getUniform("PlacementDelta")?.set(placementDelta)
    }
}
