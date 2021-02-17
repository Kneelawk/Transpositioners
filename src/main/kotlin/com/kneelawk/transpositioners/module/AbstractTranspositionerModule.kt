package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class AbstractTranspositionerModule(
    override val type: ModuleType<*>,
    override val entity: TranspositionerEntity,
    override val path: ModulePath
) : TranspositionerModule {
    protected val world: World
        get() = entity.world
    protected val attachmentPos: BlockPos
        get() = entity.decorationBlockPos
    protected val facing: Direction
        get() = entity.horizontalFacing

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