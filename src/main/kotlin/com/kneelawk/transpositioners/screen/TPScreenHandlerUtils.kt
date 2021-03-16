package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.module.Module
import com.kneelawk.transpositioners.module.ModuleContext
import com.kneelawk.transpositioners.module.ModuleInventory
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText

object TPScreenHandlerUtils {
    fun openParentScreen(module: Module, player: PlayerEntity) {
        player.openHandledScreen(
            (module.path.parent?.findModule(module.context) as? NamedScreenHandlerFactory)
                ?: when (val context = module.context) {
                    is ModuleContext.Configurator -> context.configurator
                    is ModuleContext.Entity       -> context.entity
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

    fun handleGhostSlots(
        slotNumber: Int,
        player: PlayerEntity,
        action: SlotActionType,
        button: Int,
        screenHandler: ScreenHandler,
        ghostInventory: Inventory
    ): ItemStack? {
        if (slotNumber >= 0 && slotNumber < screenHandler.slots.size) {
            val playerInventory = player.inventory
            val slot = screenHandler.getSlot(slotNumber)
            var syncStack = ItemStack.EMPTY
            if (slot != null && slot.inventory == ghostInventory) {
                if ((action == SlotActionType.PICKUP || action == SlotActionType.QUICK_MOVE) && slot.hasStack()) {
                    syncStack = slot.stack
                }

                if (action == SlotActionType.PICKUP
                    || (action == SlotActionType.QUICK_CRAFT && SyncedGuiDescription.unpackQuickCraftStage(button) == 1)
                ) {
                    val stack = playerInventory.cursorStack.copy()
                    if (!stack.isEmpty) {
                        stack.count = 1
                    }
                    slot.stack = stack
                }

                if (action == SlotActionType.QUICK_MOVE) {
                    slot.stack = ItemStack.EMPTY
                }

                return syncStack
            }

            if (action == SlotActionType.QUICK_MOVE) {
                val stack = slot.stack.copy()
                stack.count = 1
                for (target in screenHandler.slots) {
                    if (target.inventory == ghostInventory && !target.hasStack() && target.canInsert(stack)) {
                        target.stack = stack
                        return syncStack
                    }
                }

                return ItemStack.EMPTY
            }
        }

        return null
    }
}