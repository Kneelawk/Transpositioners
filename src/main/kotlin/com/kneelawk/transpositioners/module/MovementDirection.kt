package com.kneelawk.transpositioners.module

enum class MovementDirection {
    FORWARD,
    BACKWARD;

    companion object {
        fun byId(id: Int): MovementDirection {
            return values()[id.coerceIn(0, 1)]
        }
    }

    val id = ordinal
}