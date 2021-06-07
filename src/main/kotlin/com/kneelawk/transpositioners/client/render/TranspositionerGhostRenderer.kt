package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.client.entity.TranspositionerEntityRenderer
import com.kneelawk.transpositioners.item.TranspositionerItem
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Matrix4f

object TranspositionerGhostRenderer {
    private val PLACEMENT_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)
    private val GHOST_DEPTH_RANGE = Matrix4f.scale(1f, 1f, 0.01f)

    private var placementDelta = 0f
    private val renderLayers = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>()

    private fun ghostStart() {
        RenderSystem.enableTexture()
        val textureManager = MinecraftClient.getInstance().textureManager
        textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, true)
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
        MinecraftClient.getInstance().gameRenderer.lightmapTextureManager.enable()
        TPShaders.GHOST.findUniformMat4("DepthRangeMat").set(GHOST_DEPTH_RANGE)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )
        RenderSystem.setShader(TPShaders.GHOST::getProgram)
    }

    private fun ghostEnd() {
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableCull()
        MinecraftClient.getInstance().gameRenderer.lightmapTextureManager.disable()
    }

    val GHOST = object : RenderLayer(
        "TRANSPOSITIONER_GHOST",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        ::ghostStart,
        ::ghostEnd
    ) {}

    private fun placementStart() {
        RenderSystem.enableTexture()
        val textureManager = MinecraftClient.getInstance().textureManager
        textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, true)
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
        MinecraftClient.getInstance().gameRenderer.lightmapTextureManager.enable()
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )
        TPShaders.PLACEMENT.findUniform1f("PlacementDelta").set(placementDelta)
        TPShaders.PLACEMENT.findUniformMat4("DepthRangeMat").set(PLACEMENT_DEPTH_RANGE)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.setShader(TPShaders.PLACEMENT::getProgram)
    }

    private fun placementEnd() {
        RenderSystem.disableCull()
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
        MinecraftClient.getInstance().gameRenderer.lightmapTextureManager.disable()
    }

    val PLACEMENT = object : RenderLayer(
        "TRANSPOSITIONER_PLACEMENT",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        ::placementStart,
        ::placementEnd
    ) {}

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

        CONSUMERS.draw()
    }
}