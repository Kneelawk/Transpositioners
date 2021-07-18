package com.kneelawk.transpositioners.util

import net.minecraft.item.ItemStack

class BasicStackContainer private constructor(val stack: ItemStack) {
    companion object {
        fun of(stack: ItemStack): BasicStackContainer {
            if (stack.isEmpty)
                throw IllegalArgumentException("Contained stack cannot be empty")
            val newStack = stack.copy()
            newStack.count = 1
            newStack.nbt = null
            return BasicStackContainer(newStack)
        }
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other                -> true
            javaClass != other?.javaClass -> false
            else                          -> stack.isItemEqualIgnoreDamage((other as BasicStackContainer).stack)
        }
    }

    override fun hashCode(): Int {
        // shouldn't be needed because we shouldn't be keeping track of empty stacks anyways, but just in case
        if (stack.isEmpty) return 0

        return stack.item.hashCode()
    }
}