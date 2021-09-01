package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.module.Module
import com.kneelawk.transpositioners.module.ModuleContext
import com.kneelawk.transpositioners.module.ModuleInventory
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText
import java.lang.Math.floorMod

object TPScreenHandlerUtils {
    private val NON_INTEGER_STRINGS = Regex("[^0-9]+")

    fun integerStringFilter(value: String): String {
        return value.replace(NON_INTEGER_STRINGS, "")
    }

    fun openParentScreen(module: Module, player: PlayerEntity) {
        player.openHandledScreen(
            (module.path.parent?.findModule(module.context) as? NamedScreenHandlerFactory)
                ?: when (val context = module.context) {
                    is ModuleContext.Configurator -> context.configurator
                    is ModuleContext.Entity -> context.entity
                }
        )
    }

    inline fun <reified E : Enum<E>> buttonCycleEnum(currentValue: E, button: Int): E {
        val amount = when (button) {
            0 -> 1
            1 -> -1
            else -> return E::class.java.enumConstants[0]
        }
        return cycleEnum(currentValue, amount)
    }

    inline fun <reified E : Enum<E>> cycleEnum(currentValue: E): E {
        return cycleEnum(currentValue, 1)
    }

    inline fun <reified E : Enum<E>> cycleEnum(currentValue: E, amount: Int): E {
        val values = E::class.java.enumConstants
        return values[floorMod(currentValue.ordinal + amount, values.size)]
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
        val buttons = mutableListOf<WScalableButton>()
        for (i in 0 until count) {
            val button = WScalableButton(LiteralText("..."))
            button.enabled = modules.getModule(i) is NamedScreenHandlerFactory
            panel.add(button, x + i * 18, y + 18)
            buttons.add(button)
            button.onClick = {
                sendOpenModule(i)
            }
        }

        slots.addChangeListener { _, _, index, _ ->
            buttons[index - startIndex].enabled = modules.getModule(index) is NamedScreenHandlerFactory
        }
    }

    fun handleGhostSlots(
        slotNumber: Int,
        action: SlotActionType,
        button: Int,
        screenHandler: ScreenHandler,
        ghostInventory: Inventory
    ): Boolean {
        if (slotNumber >= 0 && slotNumber < screenHandler.slots.size) {
            val slot = screenHandler.getSlot(slotNumber)
            if (slot != null && slot.inventory == ghostInventory) {
                if (action == SlotActionType.PICKUP
                    || (action == SlotActionType.QUICK_CRAFT && SyncedGuiDescription.unpackQuickCraftStage(button) == 1)
                ) {
                    val stack = screenHandler.cursorStack
                    if (!stack.isEmpty) {
                        stack.count = 1
                    }
                    slot.stack = stack
                }

                if (action == SlotActionType.QUICK_MOVE) {
                    slot.stack = ItemStack.EMPTY
                }

                return true
            }

            if (action == SlotActionType.QUICK_MOVE) {
                val stack = slot.stack.copy()
                stack.count = 1
                for (target in screenHandler.slots) {
                    if (target.inventory == ghostInventory && !target.hasStack() && target.canInsert(stack)) {
                        target.stack = stack
                        return true
                    }
                }

                return true
            }
        }

        return false
    }
}