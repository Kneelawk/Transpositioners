package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.blockentity.ModuleConfiguratorBlockEntity
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

sealed class ModuleContext : ModuleContainer {
    abstract val frontPos: BlockPos
    abstract val backPos: BlockPos
    abstract val world: World
    abstract val facing: Direction

    abstract fun markDirty()

    data class Entity(val entity: TranspositionerEntity) : ModuleContext() {
        // I am moving away from the property attachmentPos because it is misleading. The attachmentPos is actually in
        // front of the transpositioner.
        override val frontPos: BlockPos
            get() = entity.decorationBlockPos
        override val backPos: BlockPos
            get() = entity.decorationBlockPos.offset(entity.horizontalFacing.opposite)
        override val world: World
            get() = entity.world
        override val facing: Direction
            get() = entity.horizontalFacing

        override fun getModule(index: Int): Module? {
            return entity.getModule(index)
        }

        override fun markDirty() {
            // Entities save differently
        }
    }

    data class Configurator(val configurator: ModuleConfiguratorBlockEntity) : ModuleContext() {
        override val frontPos: BlockPos
            get() = configurator.pos.up()
        override val backPos: BlockPos
            get() = configurator.pos
        override val world: World
            get() = configurator.world
                ?: throw RuntimeException("Attempted to get the world from a world-less configurator")
        override val facing: Direction
            get() = Direction.UP

        override fun getModule(index: Int): Module? {
            return configurator.getModule(index)
        }

        override fun markDirty() {
            configurator.markDirty()
        }
    }
}