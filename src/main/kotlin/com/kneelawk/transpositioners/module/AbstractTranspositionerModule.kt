package com.kneelawk.transpositioners.module

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class AbstractTranspositionerModule(
    override val type: ModuleType<*>,
    override val context: ModuleContext,
    override val path: ModulePath
) : TranspositionerModule {
    protected val world: World
        get() = context.world
    protected val attachmentPos: BlockPos
        get() = context.attachmentPos
    protected val facing: Direction
        get() = context.facing

    override fun getModule(index: Int): TranspositionerModule? {
        return null
    }

    override fun validate(stack: ItemStack): Boolean {
        return true
    }

    override fun writeToTag(tag: CompoundTag) {
    }

    override fun addStacksForDrop(stacks: MutableCollection<ItemStack>) {
    }

    override fun onRemove() {
    }
}