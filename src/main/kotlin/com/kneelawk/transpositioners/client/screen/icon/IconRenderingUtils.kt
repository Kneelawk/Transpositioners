package com.kneelawk.transpositioners.client.screen.icon

import com.kneelawk.transpositioners.client.render.TPRenderLayers
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Texture
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import kotlin.math.min

object IconRenderingUtils {
    fun paint(
        icon: Icon, matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int,
        horizontalAlignment: HorizontalAlignment, verticalAlignment: VerticalAlignment
    ) {
        if (icon is EnhancedIcon) {
            if (icon is ResizableIcon) {
                icon.paint(matrices, x, y, width, height)
            } else {
                val (xOffset, yOffset) = calculateOffsets(width, height, horizontalAlignment, verticalAlignment, icon.baseWidth, icon.baseHeight)
                icon.paint(matrices, x + xOffset, y + yOffset, icon.baseWidth, icon.baseHeight)
            }
        } else {
            val size = min(width, height)
            val (xOffset, yOffset) = calculateOffsets(width, height, horizontalAlignment, verticalAlignment, size, size)
            icon.paint(matrices, x + xOffset, y + yOffset, size)
        }
    }

    private fun calculateOffsets(
        width: Int, height: Int,
        horizontalAlignment: HorizontalAlignment,
        verticalAlignment: VerticalAlignment,
        iconWidth: Int,
        iconHeight: Int
    ): Pair<Int, Int> {
        val xOffset = when (horizontalAlignment) {
            HorizontalAlignment.LEFT -> 0
            HorizontalAlignment.CENTER -> (width - iconWidth) / 2
            HorizontalAlignment.RIGHT -> width - iconWidth
        }
        val yOffset = when (verticalAlignment) {
            VerticalAlignment.TOP -> 0
            VerticalAlignment.CENTER -> (height - iconHeight) / 2
            VerticalAlignment.BOTTOM -> height - iconHeight
        }
        return Pair(xOffset, yOffset)
    }

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