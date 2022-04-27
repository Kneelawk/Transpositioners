package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.module.TPModules
import com.kneelawk.transpositioners.util.typed
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
            TPItemUtils.raycast(player)?.let { entity ->
                if (context.world.isClient) {
                    ActionResult.SUCCESS
                } else {
                    if (player.isSneaking) {
                        val stack = context.stack
                        if (entity.canInsertModule(player, stack)) {
                            entity.insertModule(player, stack)
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
            if (world.isClient) {
                ActionResult.SUCCESS
            } else {
                if (user.isSneaking) {
                    if (entity.canInsertModule(user, user.getStackInHand(hand))) {
                        entity.insertModule(user, user.getStackInHand(hand))
                        ActionResult.SUCCESS
                    } else {
                        ActionResult.FAIL
                    }
                } else {
                    entity.interact(user, hand)
                }
            }
        } ?: ActionResult.FAIL).typed(user.getStackInHand(hand))
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