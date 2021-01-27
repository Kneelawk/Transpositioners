package com.kneelawk.transpositioners.client.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.SpriteAtlasTexture
import org.lwjgl.opengl.GL11

object TranspositionerGhostRenderer {
    private val texture: RenderPhase = RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, true)
    val GHOST = object : RenderLayer(
        "TRANSPOSITIONER_GHOST",
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        GL11.GL_QUADS,
        1 shl 12,
        false,
        true,
        {
            texture.startDrawing()
            RenderSystem.enableBlend()
        },
        {
            texture.endDrawing()
        }) {}
}