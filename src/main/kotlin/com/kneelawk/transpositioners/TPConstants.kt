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

    fun tt(key: String, vararg args: Any): TranslatableText {
        return TranslatableText(key, *args)
    }

    fun item(name: String, vararg args: Any): TranslatableText {
        return tt("item.$MOD_ID.$name", *args)
    }

    fun block(name: String, vararg args: Any): TranslatableText {
        return tt("block.$MOD_ID.$name", *args)
    }

    fun gui(name: String, vararg args: Any): TranslatableText {
        return tt("gui.$MOD_ID.$name", *args)
    }

    fun tooltip(name: String, vararg args: Any): TranslatableText {
        return tt("tooltip.$MOD_ID.$name", *args)
    }
}