package com.kneelawk.transpositioners.util

enum class RedstoneGateType {
    REDSTONE_DISABLE,
    REDSTONE_HIGH,
    REDSTONE_LOW,
    REDSTONE_RISING_EDGE,
    REDSTONE_FALLING_EDGE;

    companion object {
        fun byId(id: Int): RedstoneGateType {
            return values()[id.coerceIn(0, 4)]
        }
    }

    val id = ordinal
}