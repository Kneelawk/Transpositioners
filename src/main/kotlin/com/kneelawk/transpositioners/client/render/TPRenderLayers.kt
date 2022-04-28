package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.mixin.api.RenderLayerHelper
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier
import net.minecraft.util.Util

object TPRenderLayers {
    private val TRANSPOSITIONER_GHOST_SHADER = RenderPhase.Shader(TPShaders::TRANSPOSITIONER_GHOST)
    private val TRANSPOSITIONER_PLACEMENT_SHADER = RenderPhase.Shader(TPShaders::TRANSPOSITIONER_PLACEMENT)
    private val SIDE_SELECTOR_INDICATOR_SHADER = RenderPhase.Shader(TPShaders::SIDE_SELECTOR_INDICATOR)
    private val SIDE_SELECTOR_HOVER_SHADER = RenderPhase.Shader(TPShaders::SIDE_SELECTOR_HOVER)

    val TRANSPOSITIONER_GHOST: RenderLayer = RenderLayerHelper.of(
        TPConstants.str("transpositioner_ghost"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(TRANSPOSITIONER_GHOST_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )

    val TRANSPOSITIONER_PLACEMENT: RenderLayer = RenderLayerHelper.of(
        TPConstants.str("transpositioner_placement"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(TRANSPOSITIONER_PLACEMENT_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )

    val SIDE_SELECTOR_INDICATOR: RenderLayer = RenderLayerHelper.of(
        TPConstants.str("transpositioner_ghost"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(SIDE_SELECTOR_INDICATOR_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )

    val SIDE_SELECTOR_HOVER: RenderLayer = RenderLayerHelper.of(
        TPConstants.str("transpositioner_placement"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(SIDE_SELECTOR_HOVER_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )

    private val ICON = Util.memoize<Identifier, RenderLayer> { texture ->
        val multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
            .texture(RenderPhase.Texture(texture, false, false))
            .transparency(RenderLayerHelper.getTranslucentTransparency())
            .build(true)

        RenderLayerHelper.of(
            TPConstants.str("icon"), VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS, 256, true,
            false,
            multiPhaseParameters
        )
    }

    fun getIcon(texture: Identifier): RenderLayer {
        return ICON.apply(texture)
    }
}