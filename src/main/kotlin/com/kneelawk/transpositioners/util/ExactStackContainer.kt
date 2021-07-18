package com.kneelawk.transpositioners.util

import alexiil.mc.lib.attributes.item.ItemStackUtil
import net.minecraft.item.ItemStack

class ExactStackContainer private constructor(val stack: ItemStack) {
    companion object {
        fun of(stack: ItemStack): ExactStackContainer {
            if (stack.isEmpty)
                throw IllegalArgumentException("Contained stack cannot be empty")
            val newStack = stack.copy()
            newStack.count = 1
            return ExactStackContainer(newStack)
        }
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other                -> true
            javaClass != other?.javaClass -> false
            else                          -> ItemStackUtil.areEqualIgnoreAmounts(
                stack,
                (other as ExactStackContainer).stack
            )
        }
    }

    override fun hashCode(): Int {
        // shouldn't be needed because we shouldn't be keeping track of empty stacks anyways, but just in case
        if (stack.isEmpty) return 0

        var result = stack.item.hashCode()
        result = 31 * result + stack.nbt.hashCode()
        return result
    }
}