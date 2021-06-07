package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.TPConstants.identifier
import ladysnake.satin.api.managed.ShaderEffectManager
import net.minecraft.client.render.VertexFormats

object TPShaders {
    val GHOST = ShaderEffectManager.getInstance().manageCoreShader(
        identifier("transpositioner_ghost"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )
    val PLACEMENT = ShaderEffectManager.getInstance().manageCoreShader(
        identifier("transpositioner_placement"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )

    fun register() {
        // Make sure the shader effects actually get loaded before they're needed
    }
}
