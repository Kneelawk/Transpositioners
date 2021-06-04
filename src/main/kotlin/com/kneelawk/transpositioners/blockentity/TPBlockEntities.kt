package com.kneelawk.transpositioners.blockentity

import com.google.common.collect.Sets
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.block.TPBlocks
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry

object TPBlockEntities {

    val MODULE_CONFIGURATOR =
        newBlockEntityType(::ModuleConfiguratorBlockEntity, TPBlocks.MODULE_CONFIGURATOR)

    fun register() {
        register(MODULE_CONFIGURATOR, "module_configurator")
    }

    private fun register(type: BlockEntityType<*>, name: String) {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TPConstants.identifier(name), type)
    }

    private fun <T : BlockEntity> newBlockEntityType(factory: (BlockPos, BlockState) -> T, vararg blocks: Block): BlockEntityType<T> {
        return BlockEntityType(factory, Sets.newHashSet(*blocks), null)
    }
}