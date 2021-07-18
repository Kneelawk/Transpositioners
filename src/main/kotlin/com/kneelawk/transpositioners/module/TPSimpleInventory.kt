package com.kneelawk.transpositioners.module

import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList

class TPSimpleInventory(size: Int) : SimpleInventory(size) {
    override fun readNbtList(tags: NbtList) {
        for (j in 0 until size()) {
            setStack(j, ItemStack.EMPTY)
        }

        for (j in 0 until tags.size) {
            val compoundTag = tags.getCompound(j)
            val k = compoundTag.getByte("Slot").toInt() and 0xFF
            if (k >= 0 && k < size()) {
                setStack(k, ItemStack.fromNbt(compoundTag))
            }
        }
    }

    override fun toNbtList(): NbtList {
        val listTag = NbtList()

        for (i in 0 until size()) {
            val itemStack = getStack(i)
            if (!itemStack.isEmpty) {
                val compoundTag = NbtCompound()
                compoundTag.putByte("Slot", i.toByte())
                itemStack.writeNbt(compoundTag)
                listTag.add(compoundTag)
            }
        }

        return listTag
    }
}