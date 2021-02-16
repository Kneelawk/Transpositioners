package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.TranspositionersConstants
import com.mojang.serialization.Lifecycle
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry

object TranspositionerModules {
    private val moversId = TranspositionersConstants.identifier("mover_module_type")
    private val moversKey = RegistryKey.ofRegistry<ModuleType<out MoverModule>>(moversId)
    private val moversRegistry = SimpleRegistry(moversKey, Lifecycle.experimental())
    private val moversItemMap = mutableMapOf<Item, ModuleType<out MoverModule>>()

    fun register() {
        Registry.register(Registry.REGISTRIES as Registry<in Registry<*>>, moversId, moversRegistry)
    }

    fun registerMover(type: ModuleType<out MoverModule>, item: Item) {
        Registry.register(moversRegistry, type.id, type)
        moversItemMap[item] = type
    }

    fun getMoverById(id: Identifier): ModuleType<out MoverModule> =
        moversRegistry[id] ?: throw IllegalArgumentException("Unknown module type id: $id")

    fun getMoverByItem(item: Item): ModuleType<out MoverModule> =
        moversItemMap[item] ?: throw IllegalArgumentException("Unknown module type item: $item")
}