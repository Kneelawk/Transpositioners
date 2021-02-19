package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.blockentity.ModuleConfiguratorBlockEntity
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

sealed class ModuleContext : ModuleContainer {
    abstract val attachmentPos: BlockPos
    abstract val world: World
    abstract val facing: Direction

    data class Entity(val entity: TranspositionerEntity) : ModuleContext() {
        override val attachmentPos: BlockPos
            get() = entity.decorationBlockPos
        override val world: World
            get() = entity.world
        override val facing: Direction
            get() = entity.horizontalFacing

        override fun getModule(index: Int): Module? {
            return entity.getModule(index)
        }
    }

    data class Configurator(val configurator: ModuleConfiguratorBlockEntity) : ModuleContext() {
        override val attachmentPos: BlockPos
            get() = configurator.pos
        override val world: World
            get() = configurator.world
                ?: throw RuntimeException("Attempted to get the world from a world-less configurator")
        override val facing: Direction
            get() = throw RuntimeException("Attempted to get the facing direction of a configurator")

        override fun getModule(index: Int): Module? {
            return configurator.getModule(index)
        }
    }
}