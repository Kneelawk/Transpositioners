package com.kneelawk.transpositioners.util

import com.kneelawk.transpositioners.TPConstants.tooltip
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Direction

object TooltipUtils {
    fun movementDirection(direction: MovementDirection): Text {
        return tooltip(
            "direction",
            tooltip(direction.name.toLowerCase()).apply {
                when (direction) {
                    MovementDirection.FORWARD  -> formatted(Formatting.GREEN)
                    MovementDirection.BACKWARD -> formatted(Formatting.BLUE)
                }
            })
    }

    fun direction(direction: Direction): Text {
        return tooltip(direction.getName()).apply {
            when (direction) {
                Direction.DOWN  -> formatted(Formatting.GRAY)
                Direction.UP    -> formatted(Formatting.WHITE)
                Direction.NORTH -> formatted(Formatting.DARK_RED)
                Direction.SOUTH -> formatted(Formatting.DARK_BLUE)
                Direction.WEST  -> formatted(Formatting.GREEN)
                Direction.EAST  -> formatted(Formatting.YELLOW)
            }
        }
    }

    fun insertionSide(insertionSide: Direction) = tooltip(
        "insertion_side",
        direction(insertionSide)
    )

    fun extractionSide(extractionSide: Direction) = tooltip(
        "extraction_side",
        direction(extractionSide)
    )

    fun listGateType(type: ListGateType): Text {
        return tooltip("list_gate_type", tooltip(type.name.toLowerCase()).apply {
            when (type) {
                ListGateType.ALLOW -> formatted(Formatting.GREEN)
                ListGateType.DENY  -> formatted(Formatting.RED)
            }
        })
    }
}