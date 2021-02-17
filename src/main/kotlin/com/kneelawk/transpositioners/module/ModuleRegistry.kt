package com.kneelawk.transpositioners.module

import net.minecraft.item.Item

class ModuleRegistry<M : TranspositionerModule> {
    private val itemMap = mutableMapOf<Item, ModuleType<out M>>()

    fun register(type: ModuleType<out M>, item: Item) {
        itemMap[item] = type
    }

    fun maybeGetByItem(item: Item): ModuleType<out M>? = itemMap[item]
}