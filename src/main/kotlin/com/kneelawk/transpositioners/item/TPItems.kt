package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.TPConstants
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.registry.Registry

object TPItems {
    val TRANSPOSITIONER_ITEM_GROUP =
        FabricItemGroupBuilder.build(TPConstants.identifier("transpositioners"), ::groupItem)

    val TRANSPOSITIONERS_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP)
    val TOOL_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP).maxCount(1)
    val MODULE_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP).maxCount(1)

    val MODULE_BASE_MK1 = Item(MODULE_ITEM_SETTINGS)
    val MODULE_BASE_MK2 = Item(MODULE_ITEM_SETTINGS)
    val MODULE_BASE_MK3 = Item(MODULE_ITEM_SETTINGS)
    val GATE_MODULE_BASE_MK1 = Item(MODULE_ITEM_SETTINGS)
    val GATE_MODULE_BASE_MK2 = Item(MODULE_ITEM_SETTINGS)
    val GATE_MODULE_BASE_MK3 = Item(MODULE_ITEM_SETTINGS)

    val TRANSPOSITIONER_MK1 = TranspositionerItem(1, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_MK2 = TranspositionerItem(2, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_MK3 = TranspositionerItem(3, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_CONFIGURATOR = TranspositionerConfiguratorItem(TOOL_ITEM_SETTINGS)

    val ITEM_MOVER_MODULE_MK1 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_MOVER_MODULE_MK2 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_MOVER_MODULE_MK3 = ModuleItem(MODULE_ITEM_SETTINGS)

    val ITEM_GATE_MODULE_MK1 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_GATE_MODULE_MK2 = ModuleItem(MODULE_ITEM_SETTINGS)

    val REDSTONE_GATE_MODULE = ModuleItem(MODULE_ITEM_SETTINGS)

    val ITEM_AND_GATE_MODULE_MK1 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_AND_GATE_MODULE_MK2 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_AND_GATE_MODULE_MK3 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_NOT_GATE_MODULE = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_OR_GATE_MODULE_MK1 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_OR_GATE_MODULE_MK2 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_OR_GATE_MODULE_MK3 = ModuleItem(MODULE_ITEM_SETTINGS)

    fun register() {
        register(MODULE_BASE_MK1, "module_base_mk1")
        register(MODULE_BASE_MK2, "module_base_mk2")
        register(MODULE_BASE_MK3, "module_base_mk3")
        register(GATE_MODULE_BASE_MK1, "gate_module_base_mk1")
        register(GATE_MODULE_BASE_MK2, "gate_module_base_mk2")
        register(GATE_MODULE_BASE_MK3, "gate_module_base_mk3")
        register(TRANSPOSITIONER_MK1, "transpositioner_mk1")
        register(TRANSPOSITIONER_MK2, "transpositioner_mk2")
        register(TRANSPOSITIONER_MK3, "transpositioner_mk3")
        register(TRANSPOSITIONER_CONFIGURATOR, "transpositioner_configurator")
        register(ITEM_MOVER_MODULE_MK1, "item_mover_module_mk1")
        register(ITEM_MOVER_MODULE_MK2, "item_mover_module_mk2")
        register(ITEM_MOVER_MODULE_MK3, "item_mover_module_mk3")
        register(ITEM_GATE_MODULE_MK1, "item_gate_module_mk1")
        register(ITEM_GATE_MODULE_MK2, "item_gate_module_mk2")
        register(REDSTONE_GATE_MODULE, "redstone_gate_module")
        register(ITEM_AND_GATE_MODULE_MK1, "item_and_gate_module_mk1")
        register(ITEM_AND_GATE_MODULE_MK2, "item_and_gate_module_mk2")
        register(ITEM_AND_GATE_MODULE_MK3, "item_and_gate_module_mk3")
        register(ITEM_NOT_GATE_MODULE, "item_not_gate_module")
        register(ITEM_OR_GATE_MODULE_MK1, "item_or_gate_module_mk1")
        register(ITEM_OR_GATE_MODULE_MK2, "item_or_gate_module_mk2")
        register(ITEM_OR_GATE_MODULE_MK3, "item_or_gate_module_mk3")

        UseBlockCallback.EVENT.register { player, _, hand, hitResult ->
            if (!player.isSpectator) {
                val stack = player.getStackInHand(hand)
                (stack?.item as? InteractionCanceler)?.let { item ->
                    val usageContext = ItemUsageContext(player, hand, hitResult)
                    if (item.shouldCancelInteraction(usageContext)) {
                        if (player.isCreative) {
                            val count = stack.count
                            val result = stack.useOnBlock(usageContext)
                            stack.count = count
                            result
                        } else {
                            stack.useOnBlock(usageContext)
                        }
                    } else {
                        null
                    }
                }
            } else {
                null
            } ?: ActionResult.PASS
        }
    }

    private fun register(item: Item, name: String) {
        Registry.register(Registry.ITEM, TPConstants.identifier(name), item)
    }

    private fun groupItem(): ItemStack {
        return ItemStack(TRANSPOSITIONER_MK1)
    }
}