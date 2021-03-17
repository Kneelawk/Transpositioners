package com.kneelawk.transpositioners.client.screen.icon

import com.kneelawk.transpositioners.TPConstants.str
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.opengl.GL11

class FramebufferIcon(override val baseWidth: Int, override val baseHeight: Int) : EnhancedIcon {
    // this should only really break down if minecraft changes to allow larger gui scales
    private val scale = 4

    private val fbWidth = baseWidth * scale
    private val fbHeight = baseHeight * scale
    val framebuffer = Framebuffer(fbWidth, fbHeight, false, MinecraftClient.IS_SYSTEM_MAC)

    private fun renderStart() {
        framebuffer.beginRead()
    }

    private fun renderEnd() {
        framebuffer.endRead()
    }

    private val renderLayer = object : RenderLayer(
        str("framebuffer_icon"), VertexFormats.POSITION_TEXTURE, GL11.GL_QUADS, 1 shl 12, false, true, ::renderStart,
        ::renderEnd
    ) {}

    override fun paint(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        framebuffer.beginRead()
        val tess = Tessellator.getInstance()
        val buf = tess.buffer
        buf.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE)
        buf.vertex(matrices.peek().model, x.toFloat(), y.toFloat(), 0.0f).texture(0f, 1f).next()
        buf.vertex(matrices.peek().model, x.toFloat(), (y + height).toFloat(), 0.0f).texture(0f, 0f).next()
        buf.vertex(matrices.peek().model, (x + width).toFloat(), (y + height).toFloat(), 0.0f).texture(1f, 0f).next()
        buf.vertex(matrices.peek().model, (x + width).toFloat(), y.toFloat(), 0.0f).texture(1f, 1f).next()
        framebuffer.endRead()
    }

    override fun paint(
        matrices: MatrixStack, consumers: VertexConsumerProvider, x: Int, y: Int, width: Int, height: Int
    ) {
        val buf = consumers.getBuffer(renderLayer)
        buf.vertex(matrices.peek().model, x.toFloat(), y.toFloat(), 0.0f).texture(0f, 1f).next()
        buf.vertex(matrices.peek().model, x.toFloat(), (y + height).toFloat(), 0.0f).texture(0f, 0f).next()
        buf.vertex(matrices.peek().model, (x + width).toFloat(), (y + height).toFloat(), 0.0f).texture(1f, 0f).next()
        buf.vertex(matrices.peek().model, (x + width).toFloat(), y.toFloat(), 0.0f).texture(1f, 1f).next()
    }
}