package com.kneelawk.transpositioners.module

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class AbstractModule(
    override val type: ModuleType<*>,
    override val context: ModuleContext,
    override val path: ModulePath
) : Module {
    protected val world: World
        get() = context.world
    protected val frontPos: BlockPos
        get() = context.frontPos
    protected val backPos: BlockPos
        get() = context.backPos
    protected val facing: Direction
        get() = context.facing

    override fun getModule(index: Int): Module? {
        return null
    }

    override fun validate(stack: ItemStack): Boolean {
        return true
    }

    override fun writeToTag(tag: NbtCompound) {
    }

    override fun addStacksForDrop(stacks: MutableCollection<ItemStack>) {
    }

    override fun onRemove() {
    }

    fun markDirty() {
        context.markDirty()
    }
}