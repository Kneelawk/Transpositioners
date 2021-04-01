package com.kneelawk.transpositioners.client.util

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.identifier
import net.minecraft.client.util.ModelIdentifier

object TPModels {
    val TRANSPOSITIONERS = arrayOf(
        ModelIdentifier(identifier("transpositioner_mk1"), ""),
        ModelIdentifier(identifier("transpositioner_mk2"), ""),
        ModelIdentifier(identifier("transpositioner_mk3"), "")
    )

    val SIDE_SELECTS = arrayOf(
        ModelIdentifier(identifier("select_down"), ""),
        ModelIdentifier(identifier("select_up"), ""),
        ModelIdentifier(identifier("select_north"), ""),
        ModelIdentifier(identifier("select_south"), ""),
        ModelIdentifier(identifier("select_west"), ""),
        ModelIdentifier(identifier("select_east"), ""),
    )
}