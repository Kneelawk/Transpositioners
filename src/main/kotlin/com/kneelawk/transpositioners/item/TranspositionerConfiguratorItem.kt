package com.kneelawk.transpositioners.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World


class TranspositionerConfiguratorItem(settings: Settings) : Item(settings), InteractionCanceler, TranspositionerViewer {
    override fun shouldCancelInteraction(usageContext: ItemUsageContext): Boolean {
        return true
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        return context.player?.let {
            if (TranspositionerItemUtils.tryOpenTranspositioner(context.world, it)) ActionResult.SUCCESS else null
        } ?: ActionResult.FAIL
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        return if (
            TranspositionerItemUtils.tryOpenTranspositioner(world, user)
        ) TypedActionResult.success(stack) else TypedActionResult.fail(stack)
    }
}