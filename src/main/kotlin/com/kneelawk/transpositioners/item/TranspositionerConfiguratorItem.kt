package com.kneelawk.transpositioners.item

import net.minecraft.entity.damage.DamageSource
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
        return context.player?.let { player ->
            if (player.isSneaking) {
                TPItemUtils.raycast(player)?.let { entity ->
                    if (!context.world.isClient) {
                        entity.damage(DamageSource.player(player), 1f)
                    }
                    ActionResult.SUCCESS
                }
            } else {
                if (
                    TPItemUtils.tryOpenTranspositioner(context.world, player, context.hand)
                ) ActionResult.SUCCESS else null
            }
        } ?: ActionResult.FAIL
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        return if (user.isSneaking) {
            // shift-right-click to break
            TPItemUtils.raycast(user)?.let { entity ->
                if (!world.isClient) {
                    entity.damage(DamageSource.player(user), 1f)
                }
                TypedActionResult.success(stack)
            } ?: TypedActionResult.fail(stack)
        } else {
            if (
                TPItemUtils.tryOpenTranspositioner(world, user, hand)
            ) TypedActionResult.success(stack) else TypedActionResult.fail(stack)
        }
    }
}