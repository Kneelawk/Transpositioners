package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.client.screen.icon.EnhancedIcon
import com.kneelawk.transpositioners.client.screen.icon.NinePatchIcon
import com.kneelawk.transpositioners.client.screen.icon.ResizableIcon
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.AbstractButtonWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

class WScalableButton(
    private var label: Text? = null,
    private var icon: Icon? = null,
) : WWidget() {
    companion object {
        private val DISABLED_ICON =
            NinePatchIcon(AbstractButtonWidget.WIDGETS_LOCATION, 256, 256, 0, 46, 200, 20, 2, 2, 2, 2, true)
        private val REGULAR_ICON =
            NinePatchIcon(AbstractButtonWidget.WIDGETS_LOCATION, 256, 256, 0, 46 + 20, 200, 20, 3, 3, 3, 3, true)
        private val HOVERED_ICON =
            NinePatchIcon(AbstractButtonWidget.WIDGETS_LOCATION, 256, 256, 0, 46 + 40, 200, 20, 3, 3, 3, 3, true)
    }

    private var enabled = true
    private var alignment = HorizontalAlignment.CENTER
    private var padding = 1

    private var onClick: Runnable? = null
    private var tooltip: Text? = null

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
            DISABLED_ICON.paint(matrices, x, y, width, height)
        } else if (hovered || isFocused) {
            HOVERED_ICON.paint(matrices, x, y, width, height)
        } else {
            REGULAR_ICON.paint(matrices, x, y, width, height)
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
            onClick?.run()
        }
    }

    @Environment(EnvType.CLIENT)
    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        if (isActivationKey(ch)) {
            onClick(0, 0, 0)
        }
    }

    override fun addTooltip(builder: TooltipBuilder) {
        tooltip?.let { builder.add(it) }
    }

    /**
     * Gets the click handler of this button.
     *
     * @return the click handler
     * @since 2.2.0
     */
    fun getOnClick(): Runnable? {
        return onClick
    }

    /**
     * Sets the click handler of this button.
     *
     * @param onClick the new click handler
     * @return this button
     */
    fun setOnClick(onClick: Runnable?): WScalableButton {
        this.onClick = onClick
        return this
    }

    fun isEnabled(): Boolean {
        return enabled
    }

    fun setEnabled(enabled: Boolean): WScalableButton {
        this.enabled = enabled
        return this
    }

    fun getLabel(): Text? {
        return label
    }

    fun setLabel(label: Text?): WScalableButton {
        this.label = label
        return this
    }

    fun getAlignment(): HorizontalAlignment {
        return alignment
    }

    fun setAlignment(alignment: HorizontalAlignment): WScalableButton {
        this.alignment = alignment
        return this
    }

    fun getPadding(): Int {
        return padding
    }

    fun setPadding(padding: Int): WScalableButton {
        this.padding = padding
        return this
    }

    fun getTooltip(): Text? {
        return tooltip
    }

    fun setTooltip(tooltip: Text?): WScalableButton {
        this.tooltip = tooltip
        return this
    }

    /**
     * Gets the icon of this button.
     *
     * @return the icon
     * @since 2.2.0
     */
    fun getIcon(): Icon? {
        return icon
    }

    /**
     * Sets the icon of this button.
     *
     * @param icon the new icon
     * @return this button
     * @since 2.2.0
     */
    fun setIcon(icon: Icon?): WScalableButton {
        this.icon = icon
        return this
    }
}