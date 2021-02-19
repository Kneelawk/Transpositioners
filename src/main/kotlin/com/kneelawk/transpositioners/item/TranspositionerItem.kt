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
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class TranspositionerItem(val mk: Int, settings: Settings) : Item(settings), InteractionCanceler,
    TranspositionerViewer {
    companion object {
        fun getItem(mk: Int): TranspositionerItem {
            return when (mk) {
                1 -> TPItems.TRANSPOSITIONER_MK1
                2 -> TPItems.TRANSPOSITIONER_MK2
                3 -> TPItems.TRANSPOSITIONER_MK3
                else -> throw IllegalArgumentException("Invalid transpositioner mk: $mk")
            }
        }
    }

    override fun shouldCancelInteraction(usageContext: ItemUsageContext): Boolean {
        return true
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val side = context.side
        val pos = context.blockPos.offset(side)
        val player = context.player

        return if (player != null && !canPlaceOn(player, side, context.stack, pos)) {
            ActionResult.FAIL
        } else {
            getTranspositionerPlacement(
                context.world,
                player,
                context.blockPos,
                side,
                context.stack,
                context.hitPos
            )?.let { entity ->
                if (!context.world.isClient) {
                    entity.onPlace()
                    context.world.spawnEntity(entity)
                }
                context.stack.decrement(1)
                ActionResult.success(context.world.isClient)
            } ?: ActionResult.CONSUME
        }
    }

    fun getTranspositionerPlacement(
        world: World,
        player: PlayerEntity?,
        blockPos: BlockPos,
        side: Direction,
        stack: ItemStack,
        hitPos: Vec3d
    ): TranspositionerEntity? {
        val direction = when (side) {
            Direction.DOWN -> getPlacementDirection(
                hitPos.x,
                hitPos.z,
                Direction.DOWN,
                Direction.EAST,
                Direction.WEST,
                Direction.SOUTH,
                Direction.NORTH
            )
            Direction.UP -> getPlacementDirection(
                hitPos.x,
                hitPos.z,
                Direction.UP,
                Direction.EAST,
                Direction.WEST,
                Direction.SOUTH,
                Direction.NORTH
            )
            Direction.NORTH -> getPlacementDirection(
                hitPos.x,
                hitPos.y,
                Direction.NORTH,
                Direction.EAST,
                Direction.WEST,
                Direction.UP,
                Direction.DOWN
            )
            Direction.SOUTH -> getPlacementDirection(
                hitPos.x,
                hitPos.y,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST,
                Direction.UP,
                Direction.DOWN
            )
            Direction.WEST -> getPlacementDirection(
                hitPos.z,
                hitPos.y,
                Direction.WEST,
                Direction.SOUTH,
                Direction.NORTH,
                Direction.UP,
                Direction.DOWN
            )
            Direction.EAST -> getPlacementDirection(
                hitPos.z,
                hitPos.y,
                Direction.EAST,
                Direction.SOUTH,
                Direction.NORTH,
                Direction.UP,
                Direction.DOWN
            )
        }

        val entity = TranspositionerEntity(world, blockPos.offset(direction), direction, mk)
        stack.tag?.let { EntityType.loadFromEntityTag(world, player, entity, it) }
        return if (entity.canStayAttached()) entity else null
    }

    private fun getPlacementDirection(
        horizontal: Double,
        vertical: Double,
        center: Direction,
        hPlus: Direction,
        hMinus: Direction,
        vPlus: Direction,
        vMinus: Direction
    ): Direction {
        val h = MathHelper.fractionalPart(horizontal)
        val v = MathHelper.fractionalPart(vertical)
        return when {
            h > 12.0 / 16.0 && h > v && h > 1 - v -> hPlus
            v > 12.0 / 16.0 && v >= h && v >= 1 - h -> vPlus
            h < 4.0 / 16.0 && h < v && h < 1 - v -> hMinus
            v < 4.0 / 16.0 && v <= h && v <= 1 - h -> vMinus
            else -> center
        }
    }

    private fun canPlaceOn(player: PlayerEntity, side: Direction, stack: ItemStack, pos: BlockPos): Boolean {
        return !World.isOutOfBuildLimitVertically(pos) && player.canPlaceOn(pos, side, stack)
    }
}