package com.kneelawk.transpositioners

import net.minecraft.util.Identifier

object TranspositionersConstants {
    val MOD_ID = "transpositioners"

    fun identifier(name: String): Identifier {
        return Identifier(MOD_ID, name)
    }
}