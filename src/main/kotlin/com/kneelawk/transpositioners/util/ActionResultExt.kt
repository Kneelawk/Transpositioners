package com.kneelawk.transpositioners.util

import net.minecraft.util.ActionResult
import net.minecraft.util.TypedActionResult

fun <T> ActionResult.typed(value: T): TypedActionResult<T> {
    return TypedActionResult(this, value)
}
