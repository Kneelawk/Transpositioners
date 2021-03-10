package com.kneelawk.transpositioners.screen

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WWidget

class WRectGridPanel(private val cellWidth: Int = 18, private val cellHeight: Int = 18) : WPanel() {
    fun add(w: WWidget, x: Int, y: Int, width: Int = 1, height: Int = 1, offsetX: Int = 0, offsetY: Int = 0) {
        children.add(w)
        w.parent = this
        w.setLocation(x * cellWidth + offsetX, y * cellHeight + offsetY)
        if (w.canResize()) {
            w.setSize(width * cellWidth, height * cellHeight)
        }
        expandToFit(w)
    }
}