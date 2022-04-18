package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.client.screen.TPScreenUtils
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.impl.client.NarrationMessages
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

/*
 * Copied and modified from WTextField.
 */
class WSpecialTextField(private var suggestion: Text? = null) : WWidget() {
    companion object {
        private const val TEXT_PADDING_X = 4
        private const val TEXT_HEIGHT = 8
        private const val CURSOR_HEIGHT = 12

        private const val BACKGROUND_COLOR = -0x1000000
        private const val BORDER_COLOR_SELECTED = -0x60
        private const val BORDER_COLOR_UNSELECTED = -0x5f5f60
        private const val CURSOR_COLOR = -0x2f2f30
    }

    @Environment(EnvType.CLIENT)
    private var font: TextRenderer? = null

    var text = ""
        private set

    var maxLength = 16
        set(value) {
            field = value
            if (text.length > value) {
                setText(text.substring(0, value))
            }
        }
    var editable = true
    var tooltip: List<TPScreenUtils.TooltipLine>? = null

    private var tickCount = 0

    var disabledColor = 0x707070
    var enabledColor = 0xE0E0E0
    var suggestionColor = 0x808080

    // Index of the leftmost character to be rendered.
    private var scrollOffset = 0

    var cursor = 0
        private set

    /**
     * If not -1, select is the "anchor point" of a selection. That is, if you hit shift+left with no existing
     * selection, the selection will be anchored to where you were, but the cursor will move left, expanding the
     * selection as you continue to move left. If you move to the right, eventually you'll overtake the anchor, drop the
     * anchor at the same place and start expanding the selection rightwards instead.
     */
    private var select = -1

    var onChanged: ((String) -> Unit)? = null

    var textPredicate: ((String) -> Boolean)? = null
    var textFilter: ((String) -> String)? = null

    /**
     * Sets the text of this text field.
     * If the text is more than the [max length][.getMaxLength],
     * it'll be shortened to the max length.
     *
     * @param s the new text
     */
    fun setText(s: String) {
        setTextWithResult(s)
    }

    private fun setTextWithResult(s: String): Boolean {
        val textPredicate = textPredicate
        if (textPredicate == null || textPredicate.invoke(s)) {
            val shortened = if (s.length > maxLength) s.substring(0, maxLength) else s
            text = textFilter?.invoke(shortened) ?: shortened
            // Call change listener
            if (onChanged != null) onChanged!!.invoke(text)
            // Reset cursor if needed
            if (cursor >= text.length) cursor = text.length - 1
            return true
        }
        return false
    }

    override fun canResize(): Boolean {
        return true
    }

    override fun tick() {
        super.tick()
        tickCount++
    }

    fun setCursorPos(location: Int) {
        cursor = MathHelper.clamp(location, 0, text.length)
        scrollCursorIntoView()
    }

    @Environment(EnvType.CLIENT)
    fun scrollCursorIntoView() {
        if (scrollOffset > cursor) {
            scrollOffset = cursor
        }
        if (scrollOffset < cursor && font!!.trimToWidth(
                text.substring(scrollOffset), width - TEXT_PADDING_X * 2
            ).length + scrollOffset < cursor
        ) {
            scrollOffset = cursor
        }
        checkScrollOffset()
    }

    @Environment(EnvType.CLIENT)
    private fun checkScrollOffset() {
        val rightMostScrollOffset = text.length - font!!.trimToWidth(text, width - TEXT_PADDING_X * 2, true).length
        scrollOffset = min(rightMostScrollOffset, scrollOffset)
    }

    fun getSelection(): String? {
        if (select < 0) return null
        if (select == cursor) return null

        //Tidy some things
        if (select > text.length) select = text.length
        if (cursor < 0) cursor = 0
        if (cursor > text.length) cursor = text.length
        val start = min(select, cursor)
        val end = max(select, cursor)
        return text.substring(start, end)
    }

    @Environment(EnvType.CLIENT)
    private fun renderBox(matrices: MatrixStack?, x: Int, y: Int) {
        val borderColor = if (this.isFocused) BORDER_COLOR_SELECTED else BORDER_COLOR_UNSELECTED
        ScreenDrawing.coloredRect(matrices, x - 1, y - 1, width + 2, height + 2, borderColor)
        ScreenDrawing.coloredRect(matrices, x, y, width, height, BACKGROUND_COLOR)
    }

    @Environment(EnvType.CLIENT)
    private fun renderText(matrices: MatrixStack?, x: Int, y: Int, visibleText: String?) {
        val textColor = if (editable) enabledColor else disabledColor
        font!!.drawWithShadow(
            matrices, visibleText, (x + TEXT_PADDING_X).toFloat(), (y + (height - TEXT_HEIGHT) / 2).toFloat(), textColor
        )
    }

    @Environment(EnvType.CLIENT)
    private fun renderCursor(matrices: MatrixStack?, x: Int, y: Int, visibleText: String) {
        if (tickCount / 6 % 2 == 0) return
        if (cursor < scrollOffset) return
        if (cursor > scrollOffset + visibleText.length) return
        val cursorOffset = font!!.getWidth(visibleText.substring(0, cursor - scrollOffset))
        ScreenDrawing.coloredRect(
            matrices, x + TEXT_PADDING_X + cursorOffset, y + (height - CURSOR_HEIGHT) / 2, 1, CURSOR_HEIGHT, CURSOR_COLOR
        )
    }

    @Environment(EnvType.CLIENT)
    private fun renderSuggestion(matrices: MatrixStack?, x: Int, y: Int) {
        if (this.suggestion == null) return
        font!!.drawWithShadow(
            matrices, this.suggestion, (x + TEXT_PADDING_X).toFloat(), (y + (height - TEXT_HEIGHT) / 2).toFloat(),
            suggestionColor
        )
    }

    @Environment(EnvType.CLIENT)
    private fun renderSelection(matrices: MatrixStack, x: Int, y: Int, visibleText: String) {
        if (select == cursor || select == -1) return
        val textLength = visibleText.length
        val left = min(cursor, select)
        val right = max(cursor, select)
        if (right < scrollOffset || left > scrollOffset + textLength) return
        val normalizedLeft = max(scrollOffset, left) - scrollOffset
        val normalizedRight = min(scrollOffset + textLength, right) - scrollOffset
        val leftCaret = font!!.getWidth(visibleText.substring(0, normalizedLeft))
        val selectionWidth = font!!.getWidth(visibleText.substring(normalizedLeft, normalizedRight))
        invertedRect(matrices, x + TEXT_PADDING_X + leftCaret, y + (height - CURSOR_HEIGHT) / 2, selectionWidth, CURSOR_HEIGHT)
    }

    @Environment(EnvType.CLIENT)
    private fun renderTextField(matrices: MatrixStack, x: Int, y: Int) {
        if (font == null) font = MinecraftClient.getInstance().textRenderer
        checkScrollOffset()
        val visibleText = font!!.trimToWidth(text.substring(scrollOffset), width - 2 * TEXT_PADDING_X)
        renderBox(matrices, x, y)
        renderText(matrices, x, y, visibleText)
        if (text.isEmpty() && !this.isFocused) {
            renderSuggestion(matrices, x, y)
        }
        if (this.isFocused) {
            renderCursor(matrices, x, y, visibleText)
        }
        renderSelection(matrices, x, y, visibleText)
    }

    @Environment(EnvType.CLIENT)
    private fun invertedRect(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val model = matrices.peek().positionMatrix
        RenderSystem.setShaderColor(0.0f, 0.0f, 1.0f, 1.0f)
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.disableTexture()
        RenderSystem.enableColorLogicOp()
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE)
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
        buffer.vertex(model, x.toFloat(), (y + height).toFloat(), 0f).next()
        buffer.vertex(model, (x + width).toFloat(), (y + height).toFloat(), 0f).next()
        buffer.vertex(model, (x + width).toFloat(), y.toFloat(), 0f).next()
        buffer.vertex(model, x.toFloat(), y.toFloat(), 0f).next()
        buffer.end()
        BufferRenderer.draw(buffer)
        RenderSystem.disableColorLogicOp()
        RenderSystem.enableTexture()
    }

    fun setSuggestion(suggestion: String?): WSpecialTextField {
        this.suggestion = suggestion?.let { LiteralText(it) }
        return this
    }

    override fun canFocus(): Boolean {
        return true
    }

    override fun onFocusGained() {}

    @Environment(EnvType.CLIENT)
    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        renderTextField(matrices, x, y)
    }

    @Environment(EnvType.CLIENT)
    override fun onClick(x: Int, y: Int, button: Int): InputResult? {
        requestFocus()
        cursor = getCaretPosition(x - TEXT_PADDING_X)
        scrollCursorIntoView()
        return InputResult.PROCESSED
    }

    @Environment(EnvType.CLIENT)
    fun getCaretPosition(clickX: Int): Int {
        if (clickX < 0) return 0
        var lastPos = 0
        checkScrollOffset()
        val string = text.substring(scrollOffset)
        for (i in string.indices) {
            val w = font!!.getWidth(string[i].toString() + "")
            if (lastPos + w >= clickX) {
                if (clickX - lastPos < w / 2) {
                    return i + scrollOffset
                }
            }
            lastPos += w
        }
        return string.length
    }

    @Environment(EnvType.CLIENT)
    override fun onCharTyped(ch: Char) {
        insertText(ch.toString())
    }

    @Environment(EnvType.CLIENT)
    private fun insertText(toInsert: String) {
        val before: String
        val after: String
        if (select != -1 && select != cursor) {
            val left = min(cursor, select)
            val right = max(cursor, select)
            before = text.substring(0, left)
            after = text.substring(right)
        } else {
            before = text.substring(0, cursor)
            after = text.substring(cursor)
        }
        if (before.length + after.length + toInsert.length > maxLength) return
        if (setTextWithResult(before + toInsert + after)) {
            select = -1
            cursor = (before + toInsert).length
            scrollCursorIntoView()
        }
    }

    @Environment(EnvType.CLIENT)
    private fun copySelection() {
        val selection = getSelection()
        if (selection != null) {
            MinecraftClient.getInstance().keyboard.clipboard = selection
        }
    }

    @Environment(EnvType.CLIENT)
    private fun paste() {
        val clip = MinecraftClient.getInstance().keyboard.clipboard
        insertText(clip)
    }

    @Environment(EnvType.CLIENT)
    private fun deleteSelection() {
        val left = min(cursor, select)
        val right = max(cursor, select)
        if (setTextWithResult(text.substring(0, left) + text.substring(right))) {
            select = -1
            cursor = left
            scrollCursorIntoView()
        }
    }

    @Environment(EnvType.CLIENT)
    private fun delete(modifiers: Int, backwards: Boolean) {
        if (select == -1 || select == cursor) {
            select = skipCharaters(GLFW.GLFW_MOD_CONTROL and modifiers != 0, if (backwards) -1 else 1)
        }
        deleteSelection()
    }

    @Environment(EnvType.CLIENT)
    private fun skipCharaters(skipMany: Boolean, direction: Int): Int {
        if (direction != -1 && direction != 1) return cursor
        var position = cursor
        while (true) {
            position += direction
            if (position < 0) {
                return 0
            }
            if (position > text.length) {
                return text.length
            }
            if (!skipMany) return position
            if (position < text.length && Character.isWhitespace(text[position])) {
                return position
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun onDirectionalKey(direction: Int, modifiers: Int) {
        if (GLFW.GLFW_MOD_SHIFT and modifiers != 0) {
            if (select == -1 || select == cursor) select = cursor
            cursor = skipCharaters(GLFW.GLFW_MOD_CONTROL and modifiers != 0, direction)
        } else {
            if (select != -1) {
                cursor = if (direction < 0) min(cursor, select) else max(cursor, select)
                select = -1
            } else {
                cursor = skipCharaters(GLFW.GLFW_MOD_CONTROL and modifiers != 0, direction)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        if (!editable) return
        if (Screen.isCopy(ch)) {
            copySelection()
            return
        } else if (Screen.isPaste(ch)) {
            paste()
            return
        } else if (Screen.isSelectAll(ch)) {
            select = 0
            cursor = text.length
            return
        }
        when (ch) {
            GLFW.GLFW_KEY_DELETE -> delete(modifiers, false)
            GLFW.GLFW_KEY_BACKSPACE -> delete(modifiers, true)
            GLFW.GLFW_KEY_LEFT -> onDirectionalKey(-1, modifiers)
            GLFW.GLFW_KEY_RIGHT -> onDirectionalKey(1, modifiers)
            GLFW.GLFW_KEY_HOME, GLFW.GLFW_KEY_UP -> {
                if (GLFW.GLFW_MOD_SHIFT and modifiers == 0) {
                    select = -1
                }
                cursor = 0
            }
            GLFW.GLFW_KEY_END, GLFW.GLFW_KEY_DOWN -> {
                if (GLFW.GLFW_MOD_SHIFT and modifiers == 0) {
                    select = -1
                }
                cursor = text.length
            }
        }
        scrollCursorIntoView()
    }

    override fun addNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, TranslatableText(NarrationMessages.TEXT_FIELD_TITLE_KEY, text))
        if (suggestion != null) {
            builder.put(NarrationPart.HINT, TranslatableText(NarrationMessages.TEXT_FIELD_SUGGESTION_KEY, suggestion))
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