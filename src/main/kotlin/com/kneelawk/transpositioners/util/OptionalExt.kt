package com.kneelawk.transpositioners.util

import java.util.*

fun <T> Optional<T>.orNull(): T? {
    return orElse(null)
}
