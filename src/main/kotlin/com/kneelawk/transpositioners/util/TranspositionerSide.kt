package com.kneelawk.transpositioners.util

enum class TranspositionerSide {
    FRONT,
    BACK;

    companion object {
        fun byId(id: Int): TranspositionerSide {
            return values()[id.coerceIn(0, 1)]
        }
    }

    val id = ordinal
}