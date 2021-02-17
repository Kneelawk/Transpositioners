package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

interface ModuleType<T : TranspositionerModule> {
    fun readFromTag(entity: TranspositionerEntity, path: ModulePath, stack: ItemStack, tag: CompoundTag): T

    fun newInstance(entity: TranspositionerEntity, path: ModulePath, stack: ItemStack): T
}