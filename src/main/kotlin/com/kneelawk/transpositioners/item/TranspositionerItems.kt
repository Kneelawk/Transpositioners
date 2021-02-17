package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.TranspositionersConstants
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.registry.Registry

object TranspositionerItems {
    val TRANSPOSITIONER_ITEM_GROUP =
        FabricItemGroupBuilder.build(TranspositionersConstants.identifier("transpositioners"), ::groupItem)

    val TRANSPOSITIONERS_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP)
    val TOOL_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP).maxCount(1)
    val MODULE_ITEM_SETTINGS = Item.Settings().group(TRANSPOSITIONER_ITEM_GROUP).maxCount(1)

    val TRANSPOSITIONER_MK1 = TranspositionerItem(1, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_MK2 = TranspositionerItem(2, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_MK3 = TranspositionerItem(3, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_CONFIGURATOR = TranspositionerConfiguratorItem(TOOL_ITEM_SETTINGS)

    val ITEM_MOVER_MODULE_MK1 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_MOVER_MODULE_MK2 = ModuleItem(MODULE_ITEM_SETTINGS)
    val ITEM_MOVER_MODULE_MK3 = ModuleItem(MODULE_ITEM_SETTINGS)

    fun register() {
        register(TRANSPOSITIONER_MK1, "transpositioner_mk1")
        register(TRANSPOSITIONER_MK2, "transpositioner_mk2")
        register(TRANSPOSITIONER_MK3, "transpositioner_mk3")
        register(TRANSPOSITIONER_CONFIGURATOR, "transpositioner_configurator")
        register(ITEM_MOVER_MODULE_MK1, "item_mover_module_mk1")
        register(ITEM_MOVER_MODULE_MK2, "item_mover_module_mk2")
        register(ITEM_MOVER_MODULE_MK3, "item_mover_module_mk3")

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
        Registry.register(Registry.ITEM, TranspositionersConstants.identifier(name), item)
    }

    private fun groupItem(): ItemStack {
        return ItemStack(TRANSPOSITIONER_MK1)
    }
}