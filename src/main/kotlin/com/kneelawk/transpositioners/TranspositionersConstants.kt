package com.kneelawk.transpositioners

import net.minecraft.util.Identifier

object TranspositionersConstants {
    const val MOD_ID = "transpositioners"

    fun identifier(name: String): Identifier {
        return Identifier(MOD_ID, name)
    }

    fun str(name: String): String {
        return "$MOD_ID:$name"
    }
}