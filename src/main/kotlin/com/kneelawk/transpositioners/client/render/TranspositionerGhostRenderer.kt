package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.client.entity.TranspositionerEntityRenderer
import com.kneelawk.transpositioners.item.TranspositionerItem
import com.mojang.blaze3d.platform.GlStateManager.DstFactor
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor
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
import org.lwjgl.opengl.GL11

object TranspositionerGhostRenderer {
    private var placementDelta = 0f
    private val renderLayers = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>()

    private fun ghostStart() {
        RenderSystem.enableTexture()
        val textureManager = MinecraftClient.getInstance().textureManager
        textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, true)
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
        MinecraftClient.getInstance().gameRenderer.lightmapTextureManager.enable()
        // FIXME: GL11 is no-longer supported (but this still works somehow?)
        GL11.glDepthRange(0.0, 0.1)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
//        RenderSystem.enableBlend()
//        RenderSystem.blendFuncSeparate(
//            SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ONE_MINUS_SRC_ALPHA
//        )
        RenderSystem.setShader(GameRenderer::getRenderTypeEntityCutoutShader)
    }

    private fun ghostEnd() {
//        RenderSystem.disableBlend()
//        RenderSystem.defaultBlendFunc()
        // FIXME: GL11 is no-longer supported
        GL11.glDepthRange(0.0, 1.0)
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
        // FIXME: GL11 is no-longer supported
//        RenderSystem.enableBlend()
//        RenderSystem.enableAlphaTest()
//        RenderSystem.defaultAlphaFunc()
//        RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.CONSTANT_ALPHA, SrcFactor.ONE, DstFactor.ZERO)
//        RenderSystem.blendFunc(SrcFactor.CONSTANT_ALPHA, DstFactor.ONE_MINUS_CONSTANT_ALPHA)
//        val value = sin(placementDelta / 4f) / 4f + 0.5f
//        RenderSystem.blendColor(1f, 1f, 1f, value)
        // FIXME: GL11 is no-longer supported
        GL11.glDepthRange(0.0, 0.0)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.setShader(GameRenderer::getRenderTypeEntityCutoutShader)
    }

    private fun placementEnd() {
        RenderSystem.disableCull()
        // FIXME: GL11 is no-longer supported
        GL11.glDepthRange(0.0, 1.0)
        // FIXME: GL11 is no-longer supported
//        RenderSystem.blendColor(0f, 0f, 0f, 0f)
//        RenderSystem.disableBlend()
//        RenderSystem.defaultBlendFunc()
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