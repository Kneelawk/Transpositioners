package com.kneelawk.transpositioners.util

import net.minecraft.item.ItemStack

class DamageStackContainer private constructor(val stack: ItemStack) {
    companion object {
        fun of(stack: ItemStack): DamageStackContainer {
            if (stack.isEmpty)
                throw IllegalArgumentException("Contained stack cannot be empty")
            val newStack = stack.copy()
            newStack.count = 1
            return DamageStackContainer(newStack)
        }
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other                -> true
            javaClass != other?.javaClass -> false
            else                          -> {
                val otherStack = (other as DamageStackContainer).stack
                stack.item == otherStack.item && stack.damage == otherStack.damage
            }
        }
    }

    override fun hashCode(): Int {
        // shouldn't be needed because we shouldn't be keeping track of empty stacks anyways, but just in case
        if (stack.isEmpty) return 0

        var result = stack.item.hashCode()
        result = 31 * result + stack.damage
        return result
    }
}