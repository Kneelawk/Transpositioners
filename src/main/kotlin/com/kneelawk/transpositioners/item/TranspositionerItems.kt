package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.TranspositionersConstants
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.registry.Registry

object TranspositionerItems {
    val TRANSPOSITIONER_ITEM_GROUP = FabricItemGroupBuilder.build(TranspositionersConstants.identifier("transpositioners"), ::groupItem)

    val TRANSPOSITIONERS_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP)

    val TRANSPOSITIONER = TranspositionerItem(TRANSPOSITIONERS_ITEM_SETTINGS)

    fun register() {
        register(TRANSPOSITIONER, "transpositioner")
    }

    private fun register(item: Item, name: String) {
        Registry.register(Registry.ITEM, TranspositionersConstants.identifier(name), item)
    }

    private fun groupItem(): ItemStack {
        return ItemStack(TRANSPOSITIONER)
    }
}