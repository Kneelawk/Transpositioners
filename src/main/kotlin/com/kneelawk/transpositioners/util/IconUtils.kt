package com.kneelawk.transpositioners.util

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.client.screen.icon.EnhancedIcon
import com.kneelawk.transpositioners.client.screen.icon.EnhancedTextureIcon
import net.minecraft.util.math.Direction

object IconUtils {
    private val INSERT_DOWN = EnhancedTextureIcon(TPConstants.identifier("textures/gui/insert_down.png"), 16, 16)
    private val INSERT_UP = EnhancedTextureIcon(TPConstants.identifier("textures/gui/insert_up.png"), 16, 16)
    private val INSERT_NORTH = EnhancedTextureIcon(TPConstants.identifier("textures/gui/insert_north.png"), 16, 16)
    private val INSERT_SOUTH = EnhancedTextureIcon(TPConstants.identifier("textures/gui/insert_south.png"), 16, 16)
    private val INSERT_WEST = EnhancedTextureIcon(TPConstants.identifier("textures/gui/insert_west.png"), 16, 16)
    private val INSERT_EAST = EnhancedTextureIcon(TPConstants.identifier("textures/gui/insert_east.png"), 16, 16)

    private val EXTRACT_DOWN = EnhancedTextureIcon(TPConstants.identifier("textures/gui/extract_down.png"), 16, 16)
    private val EXTRACT_UP = EnhancedTextureIcon(TPConstants.identifier("textures/gui/extract_up.png"), 16, 16)
    private val EXTRACT_NORTH = EnhancedTextureIcon(TPConstants.identifier("textures/gui/extract_north.png"), 16, 16)
    private val EXTRACT_SOUTH = EnhancedTextureIcon(TPConstants.identifier("textures/gui/extract_south.png"), 16, 16)
    private val EXTRACT_WEST = EnhancedTextureIcon(TPConstants.identifier("textures/gui/extract_west.png"), 16, 16)
    private val EXTRACT_EAST = EnhancedTextureIcon(TPConstants.identifier("textures/gui/extract_east.png"), 16, 16)

    private val FORWARD_ICON = EnhancedTextureIcon(TPConstants.identifier("textures/gui/forward.png"), 16, 16)
    private val BACKWARD_ICON = EnhancedTextureIcon(TPConstants.identifier("textures/gui/backward.png"), 16, 16)

    val CHECK_ICON = EnhancedTextureIcon(TPConstants.identifier("textures/gui/check2.png"), 16, 16)
    val DENY_ICON = EnhancedTextureIcon(TPConstants.identifier("textures/gui/deny2.png"), 16, 16)

    fun insertionSide(direction: Direction): EnhancedIcon {
        return when (direction) {
            Direction.DOWN  -> INSERT_DOWN
            Direction.UP    -> INSERT_UP
            Direction.NORTH -> INSERT_NORTH
            Direction.SOUTH -> INSERT_SOUTH
            Direction.WEST  -> INSERT_WEST
            Direction.EAST  -> INSERT_EAST
        }
    }

    fun extractionSide(direction: Direction): EnhancedIcon {
        return when (direction) {
            Direction.DOWN  -> EXTRACT_DOWN
            Direction.UP    -> EXTRACT_UP
            Direction.NORTH -> EXTRACT_NORTH
            Direction.SOUTH -> EXTRACT_SOUTH
            Direction.WEST  -> EXTRACT_WEST
            Direction.EAST  -> EXTRACT_EAST
        }
    }

    fun movementDirection(direction: MovementDirection): EnhancedIcon {
        return when (direction) {
            MovementDirection.FORWARD  -> FORWARD_ICON
            MovementDirection.BACKWARD -> BACKWARD_ICON
        }
    }

    fun listGateType(type: ListGateType): EnhancedIcon {
        return when (type) {
            ListGateType.ALLOW -> CHECK_ICON
            ListGateType.DENY  -> DENY_ICON
        }
    }
}