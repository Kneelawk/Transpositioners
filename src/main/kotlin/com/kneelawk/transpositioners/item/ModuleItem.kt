package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.module.ModuleType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class ModuleItem(settings: Settings) : Item(settings), InteractionCanceler, TranspositionerViewer {
    override fun shouldCancelInteraction(usageContext: ItemUsageContext): Boolean {
        return true
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        return context.player?.let { player ->
            if (player.isSneaking) {
                TranspositionerItemUtils.raycast(player)?.let { entity ->
                    if (!context.world.isClient) {
                        // TODO: item insertion
//                        entity.damage(DamageSource.player(player), 1f)
                    }
                    ActionResult.SUCCESS
                }
            } else {
                if (
                    TranspositionerItemUtils.tryOpenTranspositioner(context.world, player, context.hand)
                ) ActionResult.SUCCESS else null
            }
        } ?: ActionResult.FAIL
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        return if (user.isSneaking) {
            // shift-right-click to break
            TranspositionerItemUtils.raycast(user)?.let { entity ->
                if (!world.isClient) {
                    // TODO: item insertion
//                    entity.damage(DamageSource.player(user), 1f)
                }
                TypedActionResult.success(stack)
            } ?: TypedActionResult.fail(stack)
        } else {
            if (
                TranspositionerItemUtils.tryOpenTranspositioner(world, user, hand)
            ) TypedActionResult.success(stack) else TypedActionResult.fail(stack)
        }
    }
}