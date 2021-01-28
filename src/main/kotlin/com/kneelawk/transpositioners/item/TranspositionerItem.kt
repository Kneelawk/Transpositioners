package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class TranspositionerItem(settings: Settings) : Item(settings), InteractionCanceler, TranspositionerViewer {
    override fun shouldCancelInteraction(usageContext: ItemUsageContext): Boolean {
        return true
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val direction = context.side
        val pos = context.blockPos.offset(direction)
        val player = context.player

        if (player != null && !canPlaceOn(player, direction, context.stack, pos)) {
            return ActionResult.FAIL
        } else {
            val world = context.world
            val entity = TranspositionerEntity(world, pos, direction)

            context.stack.tag?.let { EntityType.loadFromEntityTag(world, player, entity, it) }

            return if (entity.canStayAttached()) {
                if (!world.isClient) {
                    entity.onPlace()
                    world.spawnEntity(entity)
                }
                context.stack.decrement(1)
                ActionResult.success(world.isClient)
            } else {
                ActionResult.CONSUME
            }
        }
    }

    private fun canPlaceOn(player: PlayerEntity, side: Direction, stack: ItemStack, pos: BlockPos): Boolean {
        return !World.isOutOfBuildLimitVertically(pos) && player.canPlaceOn(pos, side, stack)
    }
}