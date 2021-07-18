package com.kneelawk.transpositioners.client.screen.icon

import com.kneelawk.transpositioners.client.render.TPRenderLayers
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.widget.data.Texture
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

object IconRenderingUtils {
    fun texturedRect(
        matrices: MatrixStack,
        consumers: VertexConsumerProvider,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        texture: Texture,
        color: Int,
        opacity: Float
    ) {
        texturedRect(
            matrices, consumers, x, y, width, height, texture.image, texture.u1, texture.v1, texture.u2, texture.v2,
            color, opacity
        )
    }

    fun texturedRect(
        stack: MatrixStack, consumers: VertexConsumerProvider, x: Int, y: Int, width: Int, height: Int,
        texture: Identifier, u1: Float, v1: Float, u2: Float, v2: Float, color: Int, opacity: Float
    ) {
        val model = stack.peek().model

        var adjWidth = width
        var adjHeight = height

        //float scale = 0.00390625F;
        if (adjWidth <= 0) adjWidth = 1
        if (adjHeight <= 0) adjHeight = 1
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f
        val layer = consumers.getBuffer(TPRenderLayers.getIcon(texture))
        layer.vertex(model, x.toFloat(), (y + adjHeight).toFloat(), 0.0f).color(r, g, b, opacity).texture(u1, v2).next()
        layer.vertex(model, (x + adjWidth).toFloat(), (y + adjHeight).toFloat(), 0.0f).color(r, g, b, opacity)
            .texture(u2, v2).next()
        layer.vertex(model, (x + adjWidth).toFloat(), y.toFloat(), 0.0f).color(r, g, b, opacity).texture(u2, v1).next()
        layer.vertex(model, x.toFloat(), y.toFloat(), 0.0f).color(r, g, b, opacity).texture(u1, v1).next()
    }

    fun texturedRect(
        matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int, texture: Texture, color: Int, opacity: Float
    ) {
        texturedRect(
            matrices, x, y, width, height, texture.image(), texture.u1(), texture.v1(), texture.u2(), texture.v2(),
            color, opacity
        )
    }

    fun texturedRect(
        matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int, texture: Identifier, u1: Float, v1: Float,
        u2: Float, v2: Float, color: Int, opacity: Float
    ) {
        val model = matrices.peek().model

        var adjWidth = width
        var adjHeight = height

        if (adjWidth <= 0) adjWidth = 1
        if (adjHeight <= 0) adjHeight = 1
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        RenderSystem.setShaderTexture(0, texture)
        RenderSystem.setShaderColor(r, g, b, opacity)
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        )
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        buffer.vertex(model, x.toFloat(), (y + adjHeight).toFloat(), 0f).texture(u1, v2).next()
        buffer.vertex(model, (x + adjWidth).toFloat(), (y + adjHeight).toFloat(), 0f).texture(u2, v2).next()
        buffer.vertex(model, (x + adjWidth).toFloat(), y.toFloat(), 0f).texture(u2, v1).next()
        buffer.vertex(model, x.toFloat(), y.toFloat(), 0f).texture(u1, v1).next()
        buffer.end()
        BufferRenderer.draw(buffer)
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
    }
}