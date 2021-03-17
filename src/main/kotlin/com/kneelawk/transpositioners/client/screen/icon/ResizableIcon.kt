package com.kneelawk.transpositioners.client.screen.icon

interface ResizableIcon : EnhancedIcon {
    val minWidth: Int
    val minHeight: Int

    override val baseWidth: Int
        get() = minWidth
    override val baseHeight: Int
        get() = minHeight
}