package com.kneelawk.transpositioners.module

import com.google.common.collect.ImmutableList
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.stream.Stream
import kotlin.math.min

/**
 * Inventory specifically designed for holding modules.
 *
 * @param M the type of module this inventory holds.
 * @param size the size of this inventory.
 * @param context the entity or configurator in which this inventory is contained.
 * @param path the path of the module containing this inventory.
 * @param registry the registry for this inventory's modules' type.
 */
class ModuleInventory<M : Module>(
    private val size: Int,
    private val context: ModuleContext,
    private val path: ModulePath,
    private val registry: ModuleRegistry<M>
) : Inventory, ModuleContainer {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    private val modules = MutableList<M?>(size) { null }
    private val stacks = MutableList(size) { ItemStack.EMPTY }
    private val listeners = mutableListOf<InventoryChangedListener>()

    /**
     * Adds an InventoryChangedListener that gets notified when this inventory gets changed.
     */
    fun addListener(listener: InventoryChangedListener) {
        listeners.add(listener)
    }

    /**
     * Removes an InventoryChangedListener.
     */
    fun removeListener(listener: InventoryChangedListener) {
        listeners.remove(listener)
    }

    /**
     * The maximum number of items allowed in a stack in this inventory is 1.
     */
    override fun getMaxCountPerStack(): Int {
        return 1
    }

    /**
     * Checks to see if a slot is empty.
     *
     * @param slot the slot index.
     * @return whether the specified slot is empty.
     */
    fun isSlotEmpty(slot: Int): Boolean {
        return if (slot >= 0 && slot < stacks.size) stacks[slot].isEmpty else true
    }

    /**
     * Gets the item stack in a specific slot.
     *
     * This triggers a module write to the stack in the requested slot.
     *
     * @param slot the slot index.
     * @return the stack in the specified slot.
     */
    override fun getStack(slot: Int): ItemStack {
        val stack = if (slot >= 0 && slot < stacks.size) stacks[slot] else ItemStack.EMPTY

        if (!stack.isEmpty) {
            val module = modules[slot]
            if (module != null) {
                if (registry.maybeGetByItem(stack.item) != module.type) {
                    LOGGER.warn("Encountered item in slot with different type than the module for that slot")
                    setStack(slot, ItemStack.EMPTY)
                    return ItemStack.EMPTY
                }

                TPModules.putModule(stack, module)
            } else {
                LOGGER.warn("Encountered item in slot without associated module")
                setStack(slot, ItemStack.EMPTY)
                return ItemStack.EMPTY
            }
        }

        return stack
    }

    /**
     * Gets a module at a specific index.
     *
     * @param index the index of the module.
     * @return the module at the specified index if any.
     */
    override fun getModule(index: Int): M? {
        return if (index >= 0 && index < size()) {
            modules[index]
        } else {
            null
        }
    }

    /**
     * Iterates over each module in this inventory.
     *
     * @param fn the function that is called on each module.
     */
    inline fun forEach(fn: (M) -> Unit) {
        for (i in 0 until size()) {
            getModule(i)?.let(fn)
        }
    }

    /**
     * Returns a stream of all the modules in this inventory.
     *
     * @return a stream of all the modules in this inventory.
     */
    fun moduleStream(): Stream<M?> {
        return modules.stream()
    }

    /**
     * Clears this module inventory, adding all its stacks to a list.
     *
     * Note: this triggers module encoding.
     *
     * @return a list of all stacks in this inventory.
     */
    fun clearToList(): MutableList<ItemStack> {
        val list = mutableListOf<ItemStack>()

        for (i in 0 until size()) {
            val stack = getStack(i)
            if (!stack.isEmpty) {
                list.add(stack)
            }
        }

        clear()

        return list
    }

    /**
     * Removes a stack at a specific slot from this inventory.
     *
     * Note: This triggers module encoding.
     *
     * @param slot the slot index.
     * @param amount (ignored) this method will only remove 1 item if any.
     * @return the removed stack.
     */
    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (amount == 0) {
            return ItemStack.EMPTY
        }

        // This inventory only allows for stacks with a single item so we don't have to care about stack splitting
        return removeStack(slot)
    }

    /**
     * Inserts a stack into this inventory.
     *
     * @param stack the stack to insert.
     * @return the remaining stack that couldn't be inserted.
     */
    fun addStack(stack: ItemStack): ItemStack {
        // more optimisations because we will never be merging stacks
        val itemStack = stack.copy()
        addToNewSlot(itemStack)
        return if (itemStack.isEmpty) ItemStack.EMPTY else itemStack
    }

    private fun addToNewSlot(stack: ItemStack) {
        if (registry.maybeGetByItem(stack.item) == null) {
            return
        }

        for (i in 0 until size()) {
            if (isSlotEmpty(i)) {
                setStack(i, stack.copy())
                stack.count = 0
                return
            }
        }
    }

    /**
     * Checks to see if a stack can be inserted into this inventory.
     *
     * @param stack the stack to check.
     * @return whether the stack can be inserted.
     */
    fun canInsert(stack: ItemStack): Boolean {
        if (registry.maybeGetByItem(stack.item) == null) {
            return false
        }

        for (i in 0 until size()) {
            if (isSlotEmpty(i)) {
                return true
            }
        }

        return false
    }

    /**
     * Checks to see if a stack is a valid type for this inventory.
     *
     * @param slot the slot index to check.
     * @param stack the stack to check.
     * @return whether the stack is a valid type for this inventory.
     */
    override fun isValid(slot: Int, stack: ItemStack): Boolean {
        if (stack.isEmpty) {
            return false
        }

        return registry.maybeGetByItem(stack.item) != null
    }

    /**
     * Removes a stack from this inventory.
     *
     * Note: this triggers module encoding.
     *
     * @param slot the slot index to remove from.
     * @return the removed stack.
     */
    override fun removeStack(slot: Int): ItemStack {
        val itemStack = getStack(slot)
        return if (itemStack.isEmpty) {
            ItemStack.EMPTY
        } else {
            setStack(slot, ItemStack.EMPTY)
            itemStack
        }
    }

    /**
     * Sets a stack in this inventory.
     *
     * @param slot the slot index to set the stack at.
     * @param stack the stack to set.
     */
    override fun setStack(slot: Int, stack: ItemStack) {
        modules[slot]?.onRemove()
        modules[slot] = null

        if (!stack.isEmpty) {
            val item = stack.item
            // don't let invalid items in
            val type = registry.maybeGetByItem(item) ?: return

            setModuleForStack(stack, type, slot)
        }

        stacks[slot] = stack
        if (!stack.isEmpty && stack.count > maxCountPerStack) {
            stack.count = maxCountPerStack
        }

        markDirty()
    }

    private fun setModuleForStack(
        stack: ItemStack,
        type: ModuleType<out M>,
        slot: Int
    ) {
        modules[slot] = TPModules.getModule(stack, type, context, path.child(slot))
    }

    /**
     * Gets the size of this inventory.
     *
     * @return the size of this inventory.
     */
    override fun size(): Int {
        return size
    }

    /**
     * Removes all stacks and modules from this inventory.
     */
    override fun clear() {
        for (i in 0 until size()) {
            modules[i]?.onRemove()
            modules[i] = null
            stacks[i] = ItemStack.EMPTY
        }
        markDirty()
    }

    /**
     * Checks to see if this inventory is empty.
     *
     * @return whether this inventory is empty.
     */
    override fun isEmpty(): Boolean {
        for (i in 0 until size()) {
            if (!isSlotEmpty(i)) {
                return false
            }
        }

        return true
    }

    /**
     * A player should always be able to use this inventory.
     */
    override fun canPlayerUse(player: PlayerEntity) = true

    /**
     * Notifies this inventory that its contents have changed and makes sure they are valid.
     *
     * This also notifies all InventoryChangedListeners.
     */
    override fun markDirty() {
        // make sure this inventory is in a valid state
        for (i in 0 until size()) {
            val module = modules[i]
            // this feels gross, but I can't think of a better way to do it
            val stack = stacks[i]
            val item = stack.item
            val type = registry.maybeGetByItem(item)

            if (module != null) {
                if (stack.isEmpty) {
                    module.onRemove()
                    modules[i] = null
                } else if (module.type != type) {
                    if (type != null) {
                        // remove the old module
                        module.onRemove()

                        setModuleForStack(stack, type, i)
                    } else {
                        // remove invalid stacks (possible through mod updates)
                        stacks[i] = ItemStack.EMPTY
                    }
                }
            } else {
                if (!stack.isEmpty) {
                    if (type != null) {
                        setModuleForStack(stack, type, i)
                    } else {
                        // remove invalid stacks (possible through mod updates)
                        stacks[i] = ItemStack.EMPTY
                    }
                }
            }
        }

        // notify the listeners
        for (listener in listeners) {
            listener.onInventoryChanged(this)
        }
    }

    /**
     * Loads this inventory from a list tag.
     *
     * @param tags the ListTag to load this inventory from.
     */
    fun readNbtList(tags: NbtList) {
        for (j in 0 until size()) {
            setStack(j, ItemStack.EMPTY)
        }

        for (j in 0 until tags.size) {
            val compoundTag = tags.getCompound(j)
            val k: Int = compoundTag.getByte("Slot").toInt() and 255
            if (k >= 0 && k < size()) {
                setStack(k, ItemStack.fromNbt(compoundTag))
            }
        }
    }

    /**
     * Writes this inventory to a ListTag.
     *
     * Note: this triggers module encoding.
     *
     * @return the ListTag containing the contents of this inventory.
     */
    fun toNbtList(): NbtList {
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

    /**
     * Creates a new module inventory with the given size and as many of this inventory's items and modules as will fit.
     *
     * This also copies over this inventory's listeners.
     *
     * This ModuleInventory will be in an invalid state after this method is called.
     *
     * Note: this triggers module encoding in the modules unable to fit into the new inventory.
     *
     * @param size the size of the new ModuleInventory created.
     * @return the new module inventory created as well as any items that couldn't fit.
     */
    fun convertToNewSized(size: Int): SizeConversion<M> {
        val newInv = ModuleInventory(size, context, path, registry)

        newInv.listeners.addAll(listeners)
        for (i in 0 until min(size, size())) {
            newInv.stacks[i] = stacks[i]
            newInv.modules[i] = modules[i]
        }

        newInv.markDirty()

        val builder = ImmutableList.builder<ItemStack>()
        for (i in size until size()) {
            val stack = getStack(i)
            if (!stack.isEmpty) {
                // We're only removing modules with associated stacks because getting the stack will have handled
                // removing modules without associated stacks.
                modules[i]?.onRemove()
                builder.add(stack)
            }
        }

        return SizeConversion(newInv, builder.build())
    }

    data class SizeConversion<M : Module>(
        val newInventory: ModuleInventory<M>,
        val remainingStacks: Collection<ItemStack>
    )
}