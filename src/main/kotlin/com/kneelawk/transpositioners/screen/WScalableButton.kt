package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.client.screen.TPScreenUtils
import com.kneelawk.transpositioners.client.screen.icon.EnhancedIcon
import com.kneelawk.transpositioners.client.screen.icon.ResizableIcon
import com.kneelawk.transpositioners.util.IconUtils.BUTTON_DISABLED
import com.kneelawk.transpositioners.util.IconUtils.BUTTON_HOVERED
import com.kneelawk.transpositioners.util.IconUtils.BUTTON_REGULAR
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

class WScalableButton(
    var label: Text? = null,
    var icon: Icon? = null,
) : WWidget() {

    var enabled = true
    var alignment = HorizontalAlignment.CENTER
    var padding = 1

    var onClick: (((Int)) -> Unit)? = null
    var tooltip: List<TPScreenUtils.TooltipLine>? = null

    override fun canResize(): Boolean {
        return true
    }

    override fun canFocus(): Boolean {
        return true
    }

    @Environment(EnvType.CLIENT)
    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        val hovered = mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight()
        if (!enabled) {
            BUTTON_DISABLED.paint(matrices, x, y, width, height)
        } else if (hovered || isFocused) {
            BUTTON_HOVERED.paint(matrices, x, y, width, height)
        } else {
            BUTTON_REGULAR.paint(matrices, x, y, width, height)
        }

        val icon = icon
        if (icon is EnhancedIcon) {
            if (icon is ResizableIcon) {
                icon.paint(matrices, x + padding, y + padding, width - padding * 2, height - padding * 2)
            } else {
                val xOffset = when (alignment) {
                    HorizontalAlignment.LEFT   -> padding
                    HorizontalAlignment.CENTER -> (width - icon.baseWidth) / 2
                    HorizontalAlignment.RIGHT  -> width - icon.baseWidth - padding
                }
                icon.paint(matrices, x + xOffset, y + (height - icon.baseHeight) / 2, icon.baseWidth, icon.baseHeight)
            }
        } else {
            icon?.paint(matrices, x + padding, y + padding, 16)
        }

        val label = label
        if (label != null) {
            var color = 0xE0E0E0
            if (!enabled) {
                color = 0xA0A0A0
            }

            val xOffset = if (icon != null && alignment == HorizontalAlignment.LEFT) 18 else 0
            ScreenDrawing.drawStringWithShadow(
                matrices,
                label.asOrderedText(),
                alignment,
                x + xOffset,
                y + (height - 8) / 2,
                width,
                color
            )
        }
    }

    @Environment(EnvType.CLIENT)
    override fun onClick(x: Int, y: Int, button: Int) {
        super.onClick(x, y, button)
        if (enabled && isWithinBounds(x, y)) {
            MinecraftClient.getInstance().soundManager.play(
                PositionedSoundInstance.master(
                    SoundEvents.UI_BUTTON_CLICK,
                    1.0f
                )
            )
            onClick?.invoke(button)
        }
    }

    @Environment(EnvType.CLIENT)
    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        if (isActivationKey(ch)) {
            onClick(0, 0, 0)
        }
    }

    @Environment(EnvType.CLIENT)
    override fun renderTooltip(matrices: MatrixStack, x: Int, y: Int, tX: Int, tY: Int) {
        val tooltip = tooltip ?: return

        val screen = MinecraftClient.getInstance().currentScreen
        screen as Screen
        TPScreenUtils.renderOrderedTooltip(matrices, tooltip, screen.width, screen.height, tX + x, tY + y, false)
    }
}