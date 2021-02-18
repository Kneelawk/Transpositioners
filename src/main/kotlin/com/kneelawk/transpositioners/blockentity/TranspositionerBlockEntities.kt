package com.kneelawk.transpositioners.blockentity

import com.google.common.collect.Sets
import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.block.TranspositionerBlocks
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

object TranspositionerBlockEntities {

    val MODULE_CONFIGURATOR =
        newBlockEntityType(::ModuleConfiguratorBlockEntity, TranspositionerBlocks.MODULE_CONFIGURATOR)

    fun register() {
        register(MODULE_CONFIGURATOR, "module_configurator")
    }

    private fun register(type: BlockEntityType<*>, name: String) {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TranspositionersConstants.identifier(name), type)
    }

    private fun <T : BlockEntity> newBlockEntityType(supplier: () -> T, vararg blocks: Block): BlockEntityType<T> {
        return BlockEntityType(supplier, Sets.newHashSet(*blocks), null)
    }
}