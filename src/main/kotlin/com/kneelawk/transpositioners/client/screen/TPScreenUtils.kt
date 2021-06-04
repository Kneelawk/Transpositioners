package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.client.screen.icon.EnhancedIcon
import com.kneelawk.transpositioners.mixin.impl.MouseAccessor
import com.kneelawk.transpositioners.proxy.CommonProxy
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.math.Matrix4f
import java.util.concurrent.atomic.AtomicReference

object TPScreenUtils {
    private val cursorPosition = AtomicReference<CursorPosition?>(null)

    fun presetCursorPosition() {
        val mouse = MinecraftClient.getInstance().mouse
        cursorPosition.set(CursorPosition(mouse.x, mouse.y))
    }

    fun applyCursorPosition() {
        cursorPosition.getAndSet(null)?.let { position ->
            val client = MinecraftClient.getInstance()
            val mouse = client.mouse as MouseAccessor
            mouse.setX(position.x)
            mouse.setY(position.y)
            // I don't know what 212993 means. I just copied it from Mouse#unlockCursor.
            InputUtil.setCursorParameters(client.window.handle, 212993, position.x, position.y)
        }
    }

    data class CursorPosition(val x: Double, val y: Double)

    fun fillGradient(
        matrix: Matrix4f, bufferBuilder: BufferBuilder, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, z: Int,
        colorStart: Int, colorEnd: Int
    ) {
        val startA = (colorStart shr 24 and 255).toFloat() / 255.0f
        val startR = (colorStart shr 16 and 255).toFloat() / 255.0f
        val startG = (colorStart shr 8 and 255).toFloat() / 255.0f
        val startB = (colorStart and 255).toFloat() / 255.0f
        val endA = (colorEnd shr 24 and 255).toFloat() / 255.0f
        val endR = (colorEnd shr 16 and 255).toFloat() / 255.0f
        val endG = (colorEnd shr 8 and 255).toFloat() / 255.0f
        val endB = (colorEnd and 255).toFloat() / 255.0f
        bufferBuilder.vertex(matrix, xEnd.toFloat(), yStart.toFloat(), z.toFloat())
            .color(startR, startG, startB, startA).next()
        bufferBuilder.vertex(matrix, xStart.toFloat(), yStart.toFloat(), z.toFloat())
            .color(startR, startG, startB, startA).next()
        bufferBuilder.vertex(matrix, xStart.toFloat(), yEnd.toFloat(), z.toFloat()).color(endR, endG, endB, endA).next()
        bufferBuilder.vertex(matrix, xEnd.toFloat(), yEnd.toFloat(), z.toFloat()).color(endR, endG, endB, endA).next()
    }

    fun renderOrderedTooltip(
        matrices: MatrixStack, lines: List<TooltipLine>, screenWidth: Int, screenHeight: Int, x: Int, y: Int,
        separateFirst: Boolean
    ) {
        if (lines.isNotEmpty()) {
            var width = 0
            var height = 0
            for (i in lines.indices) {
                val line = lines[i]
                val lineWidth = line.width
                if (lineWidth > width) {
                    width = lineWidth
                }
                if (lines.size > 1) {
                    height += line.height + if (separateFirst || i != 0) 2 else 0
                } else {
                    height = 8
                }
            }

            var adjX = x + 12
            var adjY = y - 12
            if (adjX + width > screenWidth) {
                adjX -= 28 + width
            }
            if (adjY + height + 6 > screenHeight) {
                adjY = screenHeight - height - 6
            }

            matrices.push()
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
            val matrix4f = matrices.peek().model

            fillGradient(
                matrix4f, bufferBuilder, adjX - 3, adjY - 4, adjX + width + 3, adjY - 3, 400, -267386864, -267386864
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX - 3, adjY + height + 3, adjX + width + 3, adjY + height + 4, 400,
                -267386864, -267386864
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX - 3, adjY - 3, adjX + width + 3, adjY + height + 3, 400, -267386864,
                -267386864
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX - 4, adjY - 3, adjX - 3, adjY + height + 3, 400, -267386864, -267386864
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX + width + 3, adjY - 3, adjX + width + 4, adjY + height + 3, 400,
                -267386864, -267386864
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX - 3, adjY - 3 + 1, adjX - 3 + 1, adjY + height + 3 - 1, 400, 1347420415,
                1344798847
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX + width + 2, adjY - 3 + 1, adjX + width + 3, adjY + height + 3 - 1, 400,
                1347420415, 1344798847
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX - 3, adjY - 3, adjX + width + 3, adjY - 3 + 1, 400, 1347420415, 1347420415
            )
            fillGradient(
                matrix4f, bufferBuilder, adjX - 3, adjY + height + 2, adjX + width + 3, adjY + height + 3, 400,
                1344798847, 1344798847
            )

            RenderSystem.enableDepthTest()
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            // TODO: Figure out what this did
//            RenderSystem.shadeModel(7425)
            bufferBuilder.end()
            BufferRenderer.draw(bufferBuilder)
//            RenderSystem.shadeModel(7424)
            RenderSystem.disableBlend()
            RenderSystem.enableTexture()

            val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)

            matrices.translate(0.0, 0.0, 400.0)
            for (s in lines.indices) {
                val line = lines[s]
                line.render(matrices, immediate, adjX, adjY)

                if (s == 0 && separateFirst) {
                    adjY += 2
                }

                adjY += 10
            }

            immediate.draw()
            matrices.pop()
        }
    }

    fun tooltipLine(text: Text): TooltipLine {
        return if (CommonProxy.INSTANCE.isClient) TooltipLine.TextLine(text.asOrderedText()) else TooltipLine.BlankLine
    }

    fun tooltipLine(icon: EnhancedIcon): TooltipLine {
        return if (CommonProxy.INSTANCE.isClient) TooltipLine.IconLine(icon) else TooltipLine.BlankLine
    }

    fun tooltipLine(): TooltipLine {
        return TooltipLine.BlankLine
    }

    sealed class TooltipLine {
        abstract val width: Int
        abstract val height: Int

        @Environment(EnvType.CLIENT)
        abstract fun render(matrices: MatrixStack, provider: VertexConsumerProvider, x: Int, y: Int)

        class TextLine(private val text: OrderedText) : TooltipLine() {
            override val width = MinecraftClient.getInstance().textRenderer.getWidth(text)
            override val height = 8

            @Environment(EnvType.CLIENT)
            override fun render(matrices: MatrixStack, provider: VertexConsumerProvider, x: Int, y: Int) {
                MinecraftClient.getInstance().textRenderer.draw(
                    text, x.toFloat(), y.toFloat(), -1, true, matrices.peek().model, provider,
                    false, 0,
                    15728880
                )
            }
        }

        class IconLine(private val icon: EnhancedIcon) : TooltipLine() {
            override val width = icon.baseWidth
            override val height = icon.baseHeight

            @Environment(EnvType.CLIENT)
            override fun render(matrices: MatrixStack, provider: VertexConsumerProvider, x: Int, y: Int) {
                icon.paint(matrices, provider, x, y, width, height)
            }
        }

        object BlankLine : TooltipLine() {
            override val width = 0
            override val height = 8

            @Environment(EnvType.CLIENT)
            override fun render(matrices: MatrixStack, provider: VertexConsumerProvider, x: Int, y: Int) {
            }
        }
    }
}