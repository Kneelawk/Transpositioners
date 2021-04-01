package com.kneelawk.transpositioners.module

import net.minecraft.item.Item

class ModuleRegistry<M : Module> {
    private val itemMap = mutableMapOf<Item, ModuleType<out M>>()

    internal fun register(type: ModuleType<out M>, item: Item) {
        itemMap[item] = type
    }

    fun containsItem(item: Item): Boolean = itemMap.containsKey(item)

    fun maybeGetByItem(item: Item): ModuleType<out M>? = itemMap[item]
}