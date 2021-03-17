package com.kneelawk.transpositioners.util

enum class ListGateType {
    ALLOW,
    DENY;

    companion object {
        fun byId(id: Int): ListGateType {
            return values()[id.coerceIn(0, 1)]
        }
    }

    val id = ordinal
}