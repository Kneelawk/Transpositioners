package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.client.screen.icon.IconRenderingUtils
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon

object TPBackgroundPainters {
    fun moduleSlot(moduleIdentifier: Icon): BackgroundPainter {
        return BackgroundPainter { matrices, left, top, panel ->
            BackgroundPainter.SLOT.paintBackground(matrices, left, top, panel)

            if (panel is WItemSlot && !panel.isBigSlot) {
                val slotsWide = panel.width / 18
                val slotsTall = panel.height / 18

                for (y in 0 until slotsTall) {
                    for (x in 0 until slotsWide) {
                        IconRenderingUtils.paint(
                            moduleIdentifier, matrices, left + x * 18 + 1, top + y * 18 + 1, 16, 16,
                            HorizontalAlignment.CENTER, VerticalAlignment.CENTER
                        )
                    }
                }
            } else {
                IconRenderingUtils.paint(
                    moduleIdentifier, matrices, left + 1, top + 1, panel.width - 2, panel.height - 2,
                    HorizontalAlignment.CENTER, VerticalAlignment.CENTER
                )
            }
        }
    }
}