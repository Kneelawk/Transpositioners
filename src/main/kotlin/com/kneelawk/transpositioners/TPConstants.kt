package com.kneelawk.transpositioners

import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

object TPConstants {
    const val MOD_ID = "transpositioners"

    fun identifier(name: String): Identifier {
        return Identifier(MOD_ID, name)
    }

    fun str(name: String): String {
        return "$MOD_ID:$name"
    }

    fun item(name: String, vararg args: Any): TranslatableText {
        return TranslatableText("item.$MOD_ID.$name", *args)
    }

    fun block(name: String, vararg args: Any): TranslatableText {
        return TranslatableText("block.$MOD_ID.$name", *args)
    }

    fun gui(name: String, vararg args: Any): TranslatableText {
        return TranslatableText("gui.$MOD_ID.$name", *args)
    }

    fun tooltip(name: String, vararg args: Any): TranslatableText {
        return TranslatableText("tooltip.$MOD_ID.$name", *args)
    }
}