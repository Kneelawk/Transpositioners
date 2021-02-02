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

    val TRANSPOSITIONER_MK1 = TranspositionerItem(1, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_MK2 = TranspositionerItem(2, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_MK3 = TranspositionerItem(3, TRANSPOSITIONERS_ITEM_SETTINGS)
    val TRANSPOSITIONER_CONFIGURATOR = TranspositionerConfiguratorItem(TRANSPOSITIONERS_ITEM_SETTINGS)

    fun register() {
        register(TRANSPOSITIONER_MK1, "transpositioner_mk1")
        register(TRANSPOSITIONER_MK2, "transpositioner_mk2")
        register(TRANSPOSITIONER_MK3, "transpositioner_mk3")
        register(TRANSPOSITIONER_CONFIGURATOR, "transpositioner_configurator")
    }

    private fun register(item: Item, name: String) {
        Registry.register(Registry.ITEM, TranspositionersConstants.identifier(name), item)

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

    private fun groupItem(): ItemStack {
        return ItemStack(TRANSPOSITIONER_MK1)
    }
}