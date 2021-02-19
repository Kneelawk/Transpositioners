package com.kneelawk.transpositioners.screen

import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WWidget

class WRectGridPanel(private val cellWidth: Int = 18, private val cellHeight: Int = 18) : WPanel() {
    fun add(w: WWidget, x: Int, y: Int) {
        children.add(w)
        w.parent = this
        w.setLocation(x * cellWidth, y * cellHeight)
        if (w.canResize()) {
            w.setSize(cellWidth, cellHeight)
        }

        expandToFit(w)
    }

    fun add(w: WWidget, x: Int, y: Int, width: Int, height: Int) {
        children.add(w)
        w.parent = this
        w.setLocation(x * cellWidth, y * cellHeight)
        if (w.canResize()) {
            w.setSize(width * cellWidth, height * cellHeight)
        }
        expandToFit(w)
    }
}