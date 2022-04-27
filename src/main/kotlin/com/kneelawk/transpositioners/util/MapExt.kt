package com.kneelawk.transpositioners.util

inline fun <K, V> MutableMap<K, V>.removeAll(predicate: (K, V) -> Boolean) {
    val toRemove = mutableListOf<K>()
    for ((key, value) in this) {
        if (predicate(key, value)) {
            toRemove.add(key)
        }
    }
    for (key in toRemove) {
        remove(key)
    }
}
