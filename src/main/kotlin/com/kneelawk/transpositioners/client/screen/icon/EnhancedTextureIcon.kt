package com.kneelawk.transpositioners.client.screen.icon

import io.github.cottonmc.cotton.gui.widget.data.Texture
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class EnhancedTextureIcon(
    val texture: Texture, override val baseWidth: Int, override val baseHeight: Int, var opacity: Float = 1f,
    var color: Int = 0xFF_FFFFFF.toInt()
) : EnhancedIcon {
    constructor(
        texture: Identifier, baseWidth: Int, baseHeight: Int, opacity: Float = 1f, color: Int = 0xFF_FFFFFF.toInt()
    ) : this(Texture(texture), baseWidth, baseHeight, opacity, color)

    @Environment(EnvType.CLIENT)
    override fun paint(
        matrices: MatrixStack, consumers: VertexConsumerProvider, x: Int, y: Int, width: Int, height: Int
    ) {
        IconRenderingUtils.texturedRect(matrices, consumers, x, y, width, height, texture, color, opacity)
    }

    @Environment(EnvType.CLIENT)
    override fun paint(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        IconRenderingUtils.texturedRect(matrices, x, y, width, height, texture, color, opacity)
    }
}