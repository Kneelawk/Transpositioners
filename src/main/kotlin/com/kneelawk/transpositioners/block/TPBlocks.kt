package com.kneelawk.transpositioners.block

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.item.TPItems
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.registry.Registry

object TPBlocks {
    private val BLOCK_SETTINGS =
        FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 10.0f).sounds(BlockSoundGroup.STONE)

    val MODULE_CONFIGURATOR = ModuleConfiguratorBlock(BLOCK_SETTINGS)

    fun register() {
        register(MODULE_CONFIGURATOR, "module_configurator", TPItems.TRANSPOSITIONERS_ITEM_SETTINGS)
    }

    private fun register(block: Block, name: String, itemSettings: Item.Settings) {
        register(block, name, BlockItem(block, itemSettings))
    }

    private fun register(block: Block, name: String, item: BlockItem) {
        val identifier = TPConstants.identifier(name)
        Registry.register(Registry.BLOCK, identifier, block)
        Registry.register(Registry.ITEM, identifier, item)
    }
}