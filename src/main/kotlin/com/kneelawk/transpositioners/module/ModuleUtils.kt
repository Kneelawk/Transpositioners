package com.kneelawk.transpositioners.module

import alexiil.mc.lib.net.IMsgReadCtx
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.screen.ModuleScreenHandler
import com.kneelawk.transpositioners.util.ListGateType
import com.kneelawk.transpositioners.util.MovementDirection
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.Direction

object ModuleUtils {
    inline fun <reified S : ModuleScreenHandler> screenHandler(
        ctx: IMsgReadCtx,
        module: Module,
        handlerFn: (S) -> Unit
    ) {
        (ctx.connection.player.currentScreenHandler as? S)?.let { handler ->
            if (handler.module === module) {
                handlerFn(handler)
            }
        }
    }

    fun movementDirectionTooltip(direction: MovementDirection): Text {
        return TPConstants.tooltip(
            "direction",
            TPConstants.tooltip(direction.name.toLowerCase()).apply {
                when (direction) {
                    MovementDirection.FORWARD  -> formatted(Formatting.GREEN)
                    MovementDirection.BACKWARD -> formatted(Formatting.BLUE)
                }
            })
    }

    fun directionTooltip(direction: Direction): Text {
        return TPConstants.tooltip(direction.getName()).apply {
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

    fun listGateTypeTooltip(type: ListGateType): Text {
        return TPConstants.tooltip(type.name.toLowerCase()).apply {
            when (type) {
                ListGateType.ALLOW -> formatted(Formatting.GREEN)
                ListGateType.DENY  -> formatted(Formatting.RED)
            }
        }
    }
}