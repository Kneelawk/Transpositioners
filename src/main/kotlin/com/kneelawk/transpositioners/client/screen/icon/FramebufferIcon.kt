package com.kneelawk.transpositioners.client.screen.icon

import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.mixin.api.RenderLayerHelper
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack

open class FramebufferIcon(override val baseWidth: Int, override val baseHeight: Int) : EnhancedIcon {
    // this should only really break down if minecraft changes to allow larger gui scales
    protected val scale = 4

    protected val fbWidth = baseWidth * scale
    protected val fbHeight = baseHeight * scale
    val framebuffer: Framebuffer = SimpleFramebuffer(fbWidth, fbHeight, true, MinecraftClient.IS_SYSTEM_MAC)

    private val texture = RenderPhase.TextureBase({ RenderSystem.setShaderTexture(0, framebuffer.colorAttachment) }, {})

    private val renderLayer = RenderLayerHelper.of(
        str("framebuffer_icon"),
        VertexFormats.POSITION_TEXTURE,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(RenderLayerHelper.getPositionTextureShader())
            .texture(texture).build(false)
    )

    override fun paint(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        val tess = Tessellator.getInstance()
        val buf = tess.buffer
        RenderSystem.setShaderTexture(0, framebuffer.colorAttachment)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
//        buf.vertex(x.toDouble(), (y + height).toDouble(), 0.0).texture(0f, 1f).next()
//        buf.vertex((x + width).toDouble(), (y + height).toDouble(), 0.0).texture(1f, 1f).next()
//        buf.vertex((x + width).toDouble(), y.toDouble(), 0.0).texture(1f, 0f).next()
//        buf.vertex(x.toDouble(), y.toDouble(), 0.0).texture(0f, 0f).next()
        buf.vertex(matrices.peek().model, x.toFloat(), y.toFloat(), 0.0f).texture(0f, 1f).next()
        buf.vertex(matrices.peek().model, x.toFloat(), (y + height).toFloat(), 0.0f).texture(0f, 0f).next()
        buf.vertex(matrices.peek().model, (x + width).toFloat(), (y + height).toFloat(), 0.0f).texture(1f, 0f).next()
        buf.vertex(matrices.peek().model, (x + width).toFloat(), y.toFloat(), 0.0f).texture(1f, 1f).next()
        buf.end()
        BufferRenderer.draw(buf)
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