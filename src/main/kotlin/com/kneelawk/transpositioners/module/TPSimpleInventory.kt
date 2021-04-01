package com.kneelawk.transpositioners.module

import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

class TPSimpleInventory(size: Int) : SimpleInventory(size) {
    override fun readTags(tags: ListTag) {
        for (j in 0 until size()) {
            setStack(j, ItemStack.EMPTY)
        }

        for (j in 0 until tags.size) {
            val compoundTag = tags.getCompound(j)
            val k = compoundTag.getByte("Slot").toInt() and 0xFF
            if (k >= 0 && k < size()) {
                setStack(k, ItemStack.fromTag(compoundTag))
            }
        }
    }

    override fun getTags(): ListTag {
        val listTag = ListTag()

        for (i in 0 until size()) {
            val itemStack = getStack(i)
            if (!itemStack.isEmpty) {
                val compoundTag = CompoundTag()
                compoundTag.putByte("Slot", i.toByte())
                itemStack.toTag(compoundTag)
                listTag.add(compoundTag)
            }
        }

        return listTag
    }
}