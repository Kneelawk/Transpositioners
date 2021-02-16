package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier

interface ModuleType<T : TranspositionerModule> {
    val id: Identifier

    fun readFromTag(entity: TranspositionerEntity, index: Int, stack: ItemStack, tag: CompoundTag): T

    fun newInstance(entity: TranspositionerEntity, index: Int, stack: ItemStack)
}