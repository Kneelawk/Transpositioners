package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.client.entity.TranspositionerEntityRenderer
import com.kneelawk.transpositioners.item.TranspositionerItem
import com.kneelawk.transpositioners.mixin.api.RenderLayerHelper
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Matrix4f

object TranspositionerGhostRenderer {
    private val PLACEMENT_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)
    private val GHOST_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)

    private val GHOST_SHADER = RenderPhase.Shader(TPShaders.GHOST::getProgram)
    private val PLACEMENT_SHADER = RenderPhase.Shader(TPShaders.PLACEMENT::getProgram)

    private val renderLayers = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>()

    val GHOST: RenderLayer = RenderLayerHelper.of(
        str("transpositioner_ghost"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(GHOST_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )

    val PLACEMENT: RenderLayer = RenderLayerHelper.of(
        str("transpositioner_placement"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(PLACEMENT_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )

    init {
        renderLayers[GHOST] = BufferBuilder(1 shl 12)
        renderLayers[PLACEMENT] = BufferBuilder(1 shl 12)
    }

    val CONSUMERS: VertexConsumerProvider.Immediate =
        VertexConsumerProvider.immediate(renderLayers, BufferBuilder(1 shl 12))

    fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            draw(context.camera(), context.tickDelta(), context.matrixStack())
        }
    }

    private fun draw(
        camera: Camera,
        tickDelta: Float,
        matrices: MatrixStack
    ) {
        var placementDelta = 0f

        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            val world = player.world
            placementDelta = world.time + tickDelta
            val target = client.crosshairTarget

            if (target is BlockHitResult && target.type == HitResult.Type.BLOCK) {
                for (hand in Hand.values()) {
                    val stack = player.getStackInHand(hand)
                    val item = stack.item
                    if (item is TranspositionerItem) {
                        val entity = item.getTranspositionerPlacement(
                            world,
                            player,
                            target.blockPos,
                            target.side,
                            stack,
                            target.pos
                        )
                        if (entity != null) {
                            val cameraPos = camera.pos
                            val entityOffset = TranspositionerEntityRenderer.getPositionOffset(entity, tickDelta)
                            val x = entity.x - cameraPos.x + entityOffset.x
                            val y = entity.y - cameraPos.y + entityOffset.y
                            val z = entity.z - cameraPos.z + entityOffset.z
                            matrices.push()
                            matrices.translate(x, y, z)
                            TranspositionerEntityRenderer.render(
                                entity,
                                tickDelta,
                                matrices,
                                CONSUMERS.getBuffer(PLACEMENT),
                                0
                            )
                            matrices.pop()
                        }

                        break
                    }
                }
            }
        }

        // Setup shader uniforms
        TPShaders.GHOST.findUniformMat4("DepthRangeMat").set(GHOST_DEPTH_RANGE)
        TPShaders.PLACEMENT.findUniform1f("PlacementDelta").set(placementDelta)
        TPShaders.PLACEMENT.findUniformMat4("DepthRangeMat").set(PLACEMENT_DEPTH_RANGE)

        CONSUMERS.draw()
    }
}