package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.TPConstants.identifier
import ladysnake.satin.api.managed.ManagedCoreShader
import ladysnake.satin.api.managed.ShaderEffectManager
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Matrix4f

object TPShaders {
    private val PLACEMENT_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)
    private val GHOST_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)

    val TRANSPOSITIONER_GHOST: ManagedCoreShader = ShaderEffectManager.getInstance().manageCoreShader(
        identifier("transpositioner_ghost"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )
    val TRANSPOSITIONER_PLACEMENT: ManagedCoreShader = ShaderEffectManager.getInstance().manageCoreShader(
        identifier("transpositioner_placement"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )
    val SIDE_SELECTOR_INDICATOR: ManagedCoreShader = ShaderEffectManager.getInstance().manageCoreShader(
        identifier("side_selector_indicator"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )
    val SIDE_SELECTOR_HOVER: ManagedCoreShader = ShaderEffectManager.getInstance().manageCoreShader(
        identifier("side_selector_hover"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )

    fun register() {
        WorldRenderEvents.START.register { ctx ->
            update(ctx.tickDelta())
        }
    }

    private fun update(tickDelta: Float) {
        var placementDelta = 0f

        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            val world = player.world
            placementDelta = world.time + tickDelta
        }

        // Setup shader uniforms
        TRANSPOSITIONER_GHOST.findUniformMat4("DepthRangeMat").set(GHOST_DEPTH_RANGE)
        TRANSPOSITIONER_PLACEMENT.findUniform1f("PlacementDelta").set(placementDelta)
        TRANSPOSITIONER_PLACEMENT.findUniformMat4("DepthRangeMat").set(PLACEMENT_DEPTH_RANGE)
        SIDE_SELECTOR_HOVER.findUniform1f("PlacementDelta").set(placementDelta)
    }
}
