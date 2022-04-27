package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.util.typed
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
            TPItemUtils.raycast(player)?.let { entity ->
                if (context.world.isClient) {
                    ActionResult.SUCCESS
                } else {
                    if (player.isSneaking) {
                        if (entity.damage(DamageSource.player(player), 1f)) {
                            ActionResult.SUCCESS
                        } else {
                            ActionResult.FAIL
                        }
                    } else {
                        entity.interact(player, context.hand)
                    }
                }
            }
        } ?: ActionResult.FAIL
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        return (TPItemUtils.raycast(user)?.let { entity ->
            if (user.isSneaking) {
                if (world.isClient) {
                    ActionResult.SUCCESS
                } else {
                    // shift-right-click to break
                    if (entity.damage(DamageSource.player(user), 1f)) {
                        ActionResult.SUCCESS
                    } else {
                        ActionResult.FAIL
                    }
                }
            } else {
                entity.interact(user, hand)
            }
        } ?: ActionResult.FAIL).typed(user.getStackInHand(hand))
    }
}