package com.kneelawk.transpositioners.client.screen.icon

import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.mixin.api.RenderLayerHelper
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.Matrix4f

object IconRenderingUtils {
    private val ICON_TRANSPARENCY = RenderPhase.Transparency(str("icon_transparency"),
        {
            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
            )
        }, {
            RenderSystem.disableBlend()
            RenderSystem.defaultBlendFunc()
        })
    private val ICON_RENDER_LAYER = Util.memoize<Identifier, RenderLayer> { texture ->
        val multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(texture, false, false))
            .transparency(ICON_TRANSPARENCY)
            .build(true)

        RenderLayerHelper.of(
            str("icon"), VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS, 256, true, false,
            multiPhaseParameters
        )
    }

    fun iconRenderLayer(texture: Identifier): RenderLayer {
        return ICON_RENDER_LAYER.apply(texture)
    }

    fun texturedRect(
        model: Matrix4f, consumers: VertexConsumerProvider, x: Int, y: Int, width: Int, height: Int,
        texture: Identifier, u1: Float, v1: Float, u2: Float, v2: Float, color: Int, opacity: Float
    ) {
        var adjWidth = width
        var adjHeight = height

        //float scale = 0.00390625F;
        if (adjWidth <= 0) adjWidth = 1
        if (adjHeight <= 0) adjHeight = 1
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f
        val layer = consumers.getBuffer(iconRenderLayer(texture))
        layer.vertex(model, x.toFloat(), (y + adjHeight).toFloat(), 0.0f).color(r, g, b, opacity).texture(u1, v2).next()
        layer.vertex(model, (x + adjWidth).toFloat(), (y + adjHeight).toFloat(), 0.0f).color(r, g, b, opacity)
            .texture(u2, v2).next()
        layer.vertex(model, (x + adjWidth).toFloat(), y.toFloat(), 0.0f).color(r, g, b, opacity).texture(u2, v1).next()
        layer.vertex(model, x.toFloat(), y.toFloat(), 0.0f).color(r, g, b, opacity).texture(u1, v1).next()
    }
}