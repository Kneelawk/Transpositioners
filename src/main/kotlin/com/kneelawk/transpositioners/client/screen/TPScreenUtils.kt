package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.mixin.impl.MouseAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import java.util.concurrent.atomic.AtomicReference

object TPScreenUtils {
    private val cursorPosition = AtomicReference<CursorPosition?>(null)

    fun presetCursorPosition() {
        val mouse = MinecraftClient.getInstance().mouse
        cursorPosition.set(CursorPosition(mouse.x, mouse.y))
    }

    fun applyCursorPosition() {
        cursorPosition.getAndSet(null)?.let { position ->
            val client = MinecraftClient.getInstance()
            val mouse = client.mouse as MouseAccessor
            mouse.setX(position.x)
            mouse.setY(position.y)
            // I don't know what 212993 means. I just copied it from Mouse#unlockCursor.
            InputUtil.setCursorParameters(client.window.handle, 212993, position.x, position.y)
        }
    }

    data class CursorPosition(val x: Double, val y: Double)
}