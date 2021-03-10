package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.module.Module
import com.kneelawk.transpositioners.module.ModuleContext
import com.kneelawk.transpositioners.module.ModuleInventory
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.LiteralText

object TPScreenHandlerUtils {
    fun openParentScreen(module: Module, player: PlayerEntity) {
        player.openHandledScreen(
            (module.path.parent?.findModule(module.context) as? NamedScreenHandlerFactory)
                ?: when (val context = module.context) {
                    is ModuleContext.Configurator -> context.configurator
                    is ModuleContext.Entity -> context.entity
                }
        )
    }

    inline fun <reified E : Enum<E>> cycleEnum(currentValue: E): E {
        val values = E::class.java.enumConstants
        return values[(currentValue.ordinal + 1) % values.size]
    }

    fun addSlots(
        panel: WPlainPanel,
        modules: ModuleInventory<*>,
        sendOpenModule: (Int) -> Unit,
        startIndex: Int,
        count: Int,
        x: Int,
        y: Int
    ) {
        val slots = WItemSlot.of(modules, startIndex, count, 1)
        panel.add(slots, x, y)
        val buttons = mutableListOf<WButton>()
        for (i in 0 until count) {
            val button = WButton(LiteralText("..."))
            button.isEnabled = modules.getModule(i) is NamedScreenHandlerFactory
            // buttons are 20 px tall
            panel.add(button, x + i * 18, y + 18 + 9 - 1)
            buttons.add(button)
            button.setOnClick {
                sendOpenModule(i)
            }
        }

        slots.addChangeListener { _, _, index, _ ->
            buttons[index - startIndex].isEnabled = modules.getModule(index) is NamedScreenHandlerFactory
        }
    }
}