package com.kneelawk.transpositioners.module

import net.minecraft.item.Item

class ModuleRegistry<M : TranspositionerModule> {
    private val itemMap = mutableMapOf<Item, ModuleType<out M>>()

    fun register(type: ModuleType<out M>, item: Item) {
        // this is also a tad janky but it's good enough for now
        if (TranspositionerModules.globalItemMap.containsKey(item)) {
            throw IllegalStateException("A module type is already registered for the item: $item")
        }
        TranspositionerModules.globalItemMap[item] = type
        itemMap[item] = type
    }

    fun maybeGetByItem(item: Item): ModuleType<out M>? = itemMap[item]
}