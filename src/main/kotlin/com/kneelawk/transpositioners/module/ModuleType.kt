package com.kneelawk.transpositioners.module

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.world.World

interface ModuleType<T : Module> {
    fun readFromTag(context: ModuleContext, path: ModulePath, stack: ItemStack, tag: NbtCompound): T

    fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): T

    fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        tooltipContext: TooltipContext,
        moduleData: NbtCompound
    ) {
    }
}