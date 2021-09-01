package com.kneelawk.transpositioners.util

import com.kneelawk.transpositioners.TPConstants.identifier
import com.kneelawk.transpositioners.client.screen.icon.EnhancedIcon
import com.kneelawk.transpositioners.client.screen.icon.EnhancedTextureIcon
import com.kneelawk.transpositioners.client.screen.icon.NinePatchIcon
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object IconUtils {
    private val INSERT_DOWN = EnhancedTextureIcon(identifier("textures/gui/insert_down.png"), 16, 16)
    private val INSERT_UP = EnhancedTextureIcon(identifier("textures/gui/insert_up.png"), 16, 16)
    private val INSERT_NORTH = EnhancedTextureIcon(identifier("textures/gui/insert_north.png"), 16, 16)
    private val INSERT_SOUTH = EnhancedTextureIcon(identifier("textures/gui/insert_south.png"), 16, 16)
    private val INSERT_WEST = EnhancedTextureIcon(identifier("textures/gui/insert_west.png"), 16, 16)
    private val INSERT_EAST = EnhancedTextureIcon(identifier("textures/gui/insert_east.png"), 16, 16)

    private val EXTRACT_DOWN = EnhancedTextureIcon(identifier("textures/gui/extract_down.png"), 16, 16)
    private val EXTRACT_UP = EnhancedTextureIcon(identifier("textures/gui/extract_up.png"), 16, 16)
    private val EXTRACT_NORTH = EnhancedTextureIcon(identifier("textures/gui/extract_north.png"), 16, 16)
    private val EXTRACT_SOUTH = EnhancedTextureIcon(identifier("textures/gui/extract_south.png"), 16, 16)
    private val EXTRACT_WEST = EnhancedTextureIcon(identifier("textures/gui/extract_west.png"), 16, 16)
    private val EXTRACT_EAST = EnhancedTextureIcon(identifier("textures/gui/extract_east.png"), 16, 16)

    private val FORWARD_ICON = EnhancedTextureIcon(identifier("textures/gui/forward.png"), 16, 16)
    private val BACKWARD_ICON = EnhancedTextureIcon(identifier("textures/gui/backward.png"), 16, 16)

    private val REDSTONE_HIGH_ICON = EnhancedTextureIcon(identifier("textures/gui/redstone_high.png"), 16, 16)
    private val REDSTONE_LOW_ICON = EnhancedTextureIcon(identifier("textures/gui/redstone_low.png"), 16, 16)
    private val REDSTONE_RISING_EDGE_ICON =
        EnhancedTextureIcon(identifier("textures/gui/redstone_rising_edge.png"), 16, 16)
    private val REDSTONE_FALLING_EDGE_ICON =
        EnhancedTextureIcon(identifier("textures/gui/redstone_falling_edge.png"), 16, 16)

    private val NOT_STATE_TRUE = EnhancedTextureIcon(identifier("textures/gui/not_state.png"), 16, 16)
    private val NOT_STATE_FALSE = EnhancedTextureIcon(identifier("textures/gui/not_state_disabled.png"), 16, 16)

    val CHECK_ICON = EnhancedTextureIcon(identifier("textures/gui/check2.png"), 16, 16)
    val DENY_ICON = EnhancedTextureIcon(identifier("textures/gui/deny2.png"), 16, 16)
    val CHECK_SMALL_ICON = EnhancedTextureIcon(identifier("textures/gui/check_small.png"), 7, 7)
    val DENY_SMALL_ICON = EnhancedTextureIcon(identifier("textures/gui/deny_small.png"), 7, 7)
    val X_ICON = EnhancedTextureIcon(identifier("textures/gui/disable.png"), 16, 16)

    val BUTTON_DISABLED =
        NinePatchIcon(Identifier("textures/gui/widgets.png"), 256, 256, 0, 46, 200, 20, 2, 2, 2, 2, true)
    val BUTTON_REGULAR =
        NinePatchIcon(Identifier("textures/gui/widgets.png"), 256, 256, 0, 46 + 20, 200, 20, 3, 3, 3, 3, true)
    val BUTTON_HOVERED =
        NinePatchIcon(Identifier("textures/gui/widgets.png"), 256, 256, 0, 46 + 40, 200, 20, 3, 3, 3, 3, true)

    val BORDER_INSET =
        NinePatchIcon(identifier("textures/gui/border_inset.png"), 16, 16, 0, 0, 16, 16, 2, 2, 2, 2, true)

    fun insertionSide(direction: Direction): EnhancedIcon {
        return when (direction) {
            Direction.DOWN -> INSERT_DOWN
            Direction.UP -> INSERT_UP
            Direction.NORTH -> INSERT_NORTH
            Direction.SOUTH -> INSERT_SOUTH
            Direction.WEST -> INSERT_WEST
            Direction.EAST -> INSERT_EAST
        }
    }

    fun extractionSide(direction: Direction): EnhancedIcon {
        return when (direction) {
            Direction.DOWN -> EXTRACT_DOWN
            Direction.UP -> EXTRACT_UP
            Direction.NORTH -> EXTRACT_NORTH
            Direction.SOUTH -> EXTRACT_SOUTH
            Direction.WEST -> EXTRACT_WEST
            Direction.EAST -> EXTRACT_EAST
        }
    }

    fun movementDirection(direction: MovementDirection): EnhancedIcon {
        return when (direction) {
            MovementDirection.FORWARD -> FORWARD_ICON
            MovementDirection.BACKWARD -> BACKWARD_ICON
        }
    }

    fun listGateType(type: ListGateType): EnhancedIcon {
        return when (type) {
            ListGateType.ALLOW -> CHECK_ICON
            ListGateType.DENY -> DENY_ICON
        }
    }

    fun redstoneGateType(type: RedstoneGateType): EnhancedIcon {
        return when (type) {
            RedstoneGateType.REDSTONE_DISABLE -> X_ICON
            RedstoneGateType.REDSTONE_HIGH -> REDSTONE_HIGH_ICON
            RedstoneGateType.REDSTONE_LOW -> REDSTONE_LOW_ICON
            RedstoneGateType.REDSTONE_RISING_EDGE -> REDSTONE_RISING_EDGE_ICON
            RedstoneGateType.REDSTONE_FALLING_EDGE -> REDSTONE_FALLING_EDGE_ICON
        }
    }

    fun transpositionerSide(side: TranspositionerSide): EnhancedIcon {
        return when (side) {
            TranspositionerSide.FRONT -> FORWARD_ICON
            TranspositionerSide.BACK -> BACKWARD_ICON
        }
    }

    fun notState(notState: Boolean): EnhancedIcon {
        return if (notState) NOT_STATE_TRUE else NOT_STATE_FALSE
    }
}