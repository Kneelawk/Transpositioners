package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

object ItemMoverModuleType : ModuleType<ItemMoverModule> {
    override fun readFromTag(
        entity: TranspositionerEntity,
        path: ModulePath,
        stack: ItemStack,
        tag: CompoundTag
    ): ItemMoverModule {
        return ItemMoverModule(entity, path)
    }

    override fun newInstance(entity: TranspositionerEntity, path: ModulePath, stack: ItemStack): ItemMoverModule {
        return ItemMoverModule(entity, path)
    }
}