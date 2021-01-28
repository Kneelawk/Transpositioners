package com.kneelawk.transpositioners.client.render

import com.mojang.blaze3d.platform.GlStateManager.DstFactor
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.minecraft.client.render.*
import net.minecraft.client.texture.SpriteAtlasTexture
import org.lwjgl.opengl.GL11

object TranspositionerGhostRenderer {
    private val renderLayers = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>()
    private val texture: RenderPhase = RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true)

    val RENDER_LAYER = object : RenderLayer(
        "TRANSPOSITIONER_GHOST",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        GL11.GL_QUADS,
        1 shl 12,
        false,
        true,
        {
            texture.startDrawing()
            RenderSystem.enableBlend()
            RenderSystem.enableAlphaTest()
            RenderSystem.defaultAlphaFunc()
            RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.CONSTANT_ALPHA, SrcFactor.ONE, DstFactor.ZERO)
            RenderSystem.blendColor(1f, 1f, 1f, 0.9f)
            GL11.glDepthRange(0.0, 0.0)
            RenderSystem.enableCull()
            RenderSystem.enableDepthTest()
        },
        {
            GL11.glDepthRange(0.0, 1.0)
            RenderSystem.blendColor(0f, 0f, 0f, 0f)
            RenderSystem.disableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()
            texture.endDrawing()
        }) {}

    init {
        renderLayers[RENDER_LAYER] = BufferBuilder(1 shl 12)
    }

    val CONSUMERS: VertexConsumerProvider.Immediate = VertexConsumerProvider.immediate(renderLayers, BufferBuilder(1 shl 12))

    fun draw() {
        CONSUMERS.draw()
    }
}