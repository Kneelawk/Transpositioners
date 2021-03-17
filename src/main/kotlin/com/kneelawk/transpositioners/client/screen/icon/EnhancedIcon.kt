package com.kneelawk.transpositioners.client.screen.icon

import io.github.cottonmc.cotton.gui.widget.icon.Icon
import net.minecraft.client.util.math.MatrixStack

interface EnhancedIcon : Icon {
    val baseWidth: Int
    val baseHeight: Int

    fun paint(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int)

    override fun paint(matrices: MatrixStack, x: Int, y: Int, size: Int) {
        paint(matrices, x, y, size, size)
    }
}