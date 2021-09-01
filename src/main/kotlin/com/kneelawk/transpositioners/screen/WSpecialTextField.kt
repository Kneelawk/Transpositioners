package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.client.screen.TPScreenUtils
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

class WSpecialTextField(private val suggestion: Text? = null) : WWidget() {
    companion object {
        const val OFFSET_X_TEXT = 4

        /**
         * From an X offset past the left edge of a TextRenderer.draw, finds out what the closest caret
         * position (division between letters) is.
         * @param s
         * @param x
         * @return
         */
        @Environment(EnvType.CLIENT)
        fun getCaretPos(s: String, x: Int): Int {
            if (x <= 0) return 0
            val font = MinecraftClient.getInstance().textRenderer
            var lastAdvance = 0
            for (i in 0 until s.length - 1) {
                val advance = font.getWidth(s.substring(0, i + 1))
                val charAdvance = advance - lastAdvance
                if (x < advance + charAdvance / 2) return i + 1
                lastAdvance = advance
            }
            return s.length
        }

        /**
         * From a caret position, finds out what the x-offset to draw the caret is.
         * @param s
         * @param pos
         * @return
         */
        @Environment(EnvType.CLIENT)
        fun getCaretOffset(s: String, pos: Int): Int {
            if (pos == 0) return 0 //-1;
            val font =
                MinecraftClient.getInstance().textRenderer
            return font.getWidth(s.substring(0, pos)) + 1 //(font.isRightToLeft()) ? -ofs : ofs;
        }
    }

    @Environment(EnvType.CLIENT)
    private var font: TextRenderer? = null

    var text = ""
        private set

    var maxLength = 16
        private set

    var editable = true

    var enabledColor = 0xE0E0E0
    var uneditableColor = 0x707070

    var cursor = 0
        private set

    /**
     * If not -1, select is the "anchor point" of a selection. That is, if you hit shift+left with
     * no existing selection, the selection will be anchored to where you were, but the cursor will
     * move left, expanding the selection as you continue to move left. If you move to the right,
     * eventually you'll overtake the anchor, drop the anchor at the same place and start expanding
     * the selection rightwards instead.
     */
    private var select = -1

    var textFilter: ((String) -> String)? = null
    var onChanged: ((String) -> Unit)? = null
    var tooltip: List<TPScreenUtils.TooltipLine>? = null

    @Environment(EnvType.CLIENT)
    private val backgroundPainter: BackgroundPainter? = null

    fun setText(value: String) {
        val textFilter = textFilter
        val newText = textFilter?.invoke(value) ?: value
        text = if (newText.length > maxLength) newText.substring(0, maxLength) else newText
        val onChanged = onChanged
        if (onChanged != null) onChanged(text)
    }

    fun setCursorPos(location: Int) {
        cursor = MathHelper.clamp(location, 0, text.length)
    }

    fun setMaxLength(max: Int) {
        maxLength = max
        if (text.length > max) {
            text = text.substring(0, max)
            onChanged?.invoke(text)
        }
    }

    override fun canResize() = true

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
    private fun renderTextField(matrices: MatrixStack, x: Int, y: Int) {
        val font = font ?: MinecraftClient.getInstance().textRenderer
        this.font = font

        val borderColor = if (this.isFocused) -0x60 else -0x5f5f60
        ScreenDrawing.coloredRect(matrices, x - 1, y - 1, width + 2, height + 2, borderColor)
        ScreenDrawing.coloredRect(matrices, x, y, width, height, -0x1000000)
        val textColor = if (editable) enabledColor else uneditableColor

        //TODO: Scroll offset
        val trimText = font!!.trimToWidth(text, width - OFFSET_X_TEXT)
        val selection = select != -1
        val focused =
            this.isFocused //this.isFocused() && this.focusedTicks / 6 % 2 == 0 && boolean_1; //Blinks the cursor

        //int textWidth = font.getStringWidth(trimText);
        //int textAnchor = (font.isRightToLeft()) ?
        //		x + OFFSET_X_TEXT + textWidth :
        //		x + OFFSET_X_TEXT;
        val textX = x + OFFSET_X_TEXT
        //(font.isRightToLeft()) ?
        //textAnchor - textWidth :
        //textAnchor;
        val textY = y + (height - 8) / 2

        //TODO: Adjust by scroll offset
        var adjustedCursor = cursor
        if (adjustedCursor > trimText.length) {
            adjustedCursor = trimText.length
        }
        var preCursorAdvance = textX
        if (trimText.isNotEmpty()) {
            val string2 = trimText.substring(0, adjustedCursor)
            preCursorAdvance = font.drawWithShadow(matrices, string2, textX.toFloat(), textY.toFloat(), textColor)
        }
        if (adjustedCursor < trimText.length) {
            font.drawWithShadow(
                matrices, trimText.substring(adjustedCursor), (preCursorAdvance - 1).toFloat(),
                textY.toFloat(), textColor
            )
        }
        if (text.isEmpty() && suggestion != null) {
            font.drawWithShadow(matrices, suggestion, textX.toFloat(), textY.toFloat(), -0x7f7f80)
        }

        //int var10002;
        //int var10003;
        if (focused && !selection) {
            if (adjustedCursor < trimText.length) {
                //int caretLoc = WTextField.getCaretOffset(text, cursor);
                //if (caretLoc<0) {
                //	caretLoc = textX+MinecraftClient.getInstance().textRenderer.getStringWidth(trimText)-caretLoc;
                //} else {
                //	caretLoc = textX+caretLoc-1;
                //}
                ScreenDrawing.coloredRect(matrices, preCursorAdvance - 1, textY - 2, 1, 12, -0x2f2f30)
                //if (boolean_3) {
                //	int var10001 = int_7 - 1;
                //	var10002 = int_9 + 1;
                //	var10003 = int_7 + 1;
                //
                //	DrawableHelper.fill(int_9, var10001, var10002, var10003 + 9, -3092272);
            } else {
                font.drawWithShadow(matrices, "_", preCursorAdvance.toFloat(), textY.toFloat(), textColor)
            }
        }
        if (selection) {
            var a = getCaretOffset(text, cursor)
            var b = getCaretOffset(text, select)
            if (b < a) {
                val tmp = b
                b = a
                a = tmp
            }
            invertedRect(matrices, textX + a - 1, textY - 1, min(b - a, width - OFFSET_X_TEXT), 12)
        }
    }

    @Environment(EnvType.CLIENT)
    private fun invertedRect(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val model = matrices.peek().model
        RenderSystem.setShaderColor(0.0f, 0.0f, 1.0f, 1.0f)
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
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

    override fun canFocus(): Boolean {
        return true
    }

    override fun onFocusGained() {}

    @Environment(EnvType.CLIENT)
    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        renderTextField(matrices!!, x, y)
    }

    @Environment(EnvType.CLIENT)
    override fun onClick(x: Int, y: Int, button: Int): InputResult {
        requestFocus()
        cursor = WTextField.getCaretPos(text, x - WTextField.OFFSET_X_TEXT)
        return InputResult.PROCESSED
    }

    @Environment(EnvType.CLIENT)
    override fun onCharTyped(ch: Char) {
        if (text.length < maxLength) {
            //snap cursor into bounds if it went astray
            if (cursor < 0) cursor = 0
            if (cursor > text.length) cursor = text.length
            val before = text.substring(0, cursor)
            val after = text.substring(cursor, text.length)
            val newText = before + ch + after
            text = textFilter?.invoke(newText) ?: newText
            cursor++
            onChanged?.invoke(text)
        }
    }

    @Environment(EnvType.CLIENT)
    override fun onKeyPressed(ch: Int, key: Int, modifiers: Int) {
        if (!editable) return
        if (Screen.isCopy(ch)) {
            val selection = getSelection()
            if (selection != null) {
                MinecraftClient.getInstance().keyboard.clipboard = selection
            }
            return
        } else if (Screen.isPaste(ch)) {
            if (select != -1) {
                var a = select
                var b = cursor
                if (b < a) {
                    val tmp = b
                    b = a
                    a = tmp
                }
                val before = text.substring(0, a)
                val after = text.substring(b)
                val clip = MinecraftClient.getInstance().keyboard.clipboard
                val newText = before + clip + after
                text = textFilter?.invoke(newText) ?: newText
                select = -1
                cursor = (before + clip).length
            } else {
                val before = text.substring(0, cursor)
                val after = text.substring(cursor, text.length)
                val clip = MinecraftClient.getInstance().keyboard.clipboard
                val newText = before + clip + after
                text = textFilter?.invoke(newText) ?: newText
                cursor += clip.length
                if (text.length > maxLength) {
                    text = text.substring(0, maxLength)
                    if (cursor > text.length) cursor = text.length
                }
            }
            onChanged?.invoke(text)
            return
        } else if (Screen.isSelectAll(ch)) {
            select = 0
            cursor = text.length
            return
        }

        //System.out.println("Ch: "+ch+", Key: "+key+", Mod: "+modifiers);
        if (modifiers == 0) {
            if (ch == GLFW.GLFW_KEY_DELETE || ch == GLFW.GLFW_KEY_BACKSPACE) {
                if (text.isNotEmpty() && cursor > 0) {
                    // Sometimes there's weirdness with the cursor ending up beyond the end of the string
                    cursor = min(cursor, text.length)

                    if (select >= 0 && select != cursor) {
                        var a = select
                        var b = cursor
                        if (b < a) {
                            val tmp = b
                            b = a
                            a = tmp
                        }

                        val before = text.substring(0, a)
                        val after = text.substring(b)

                        val newText = before + after
                        text = textFilter?.invoke(newText) ?: newText

                        if (cursor == b) cursor = a

                        select = -1
                    } else {
                        var before = text.substring(0, cursor)
                        val after = text.substring(cursor, text.length)
                        before = before.substring(0, before.length - 1)

                        val newText = before + after
                        text = textFilter?.invoke(newText) ?: newText

                        cursor--
                    }
                    onChanged?.invoke(text)
                }
            } else if (ch == GLFW.GLFW_KEY_LEFT) {
                if (select != -1) {
                    cursor = min(cursor, select)
                    select = -1 //Clear the selection anchor
                } else {
                    if (cursor > 0) cursor--
                }
            } else if (ch == GLFW.GLFW_KEY_RIGHT) {
                if (select != -1) {
                    cursor = max(cursor, select)
                    select = -1 //Clear the selection anchor
                } else {
                    if (cursor < text.length) cursor++
                }
            } else {
                //System.out.println("Ch: "+ch+", Key: "+key);
            }
        } else {
            if (modifiers == GLFW.GLFW_MOD_SHIFT) {
                if (ch == GLFW.GLFW_KEY_LEFT) {
                    if (select == -1) select = cursor
                    if (cursor > 0) cursor--
                    if (select == cursor) select = -1
                } else if (ch == GLFW.GLFW_KEY_RIGHT) {
                    if (select == -1) select = cursor
                    if (cursor < text.length) cursor++
                    if (select == cursor) select = -1
                }
            }
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