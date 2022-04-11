package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.module.TPModules
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
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
                TPItemUtils.raycast(player)?.let { entity ->
                    val stack = context.stack
                    if (entity.canInsertModule(player, stack)) {
                        entity.insertModule(player, stack)
                        ActionResult.SUCCESS
                    } else {
                        ActionResult.FAIL
                    }
                }
            } else {
                if (
                    TPItemUtils.tryOpenTranspositioner(context.world, player, context.hand)
                ) ActionResult.SUCCESS else ActionResult.PASS
            }
        } ?: ActionResult.FAIL
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        return if (user.isSneaking) {
            TPItemUtils.raycast(user)?.let { entity ->
                if (entity.canInsertModule(user, stack)) {
                    entity.insertModule(user, stack)
                    TypedActionResult.success(stack)
                } else {
                    TypedActionResult.fail(stack)
                }
            } ?: TypedActionResult.fail(stack)
        } else {
            if (
                TPItemUtils.tryOpenTranspositioner(world, user, hand)
            ) TypedActionResult.success(stack) else TypedActionResult.pass(stack)
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        TPModules.appendTooltip(stack, world, tooltip, context)
    }
}