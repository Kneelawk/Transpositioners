package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import alexiil.mc.lib.attributes.item.impl.EmptyFixedItemInv
import com.kneelawk.transpositioners.util.ExactStackContainer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ItemMoverMk1Module(context: ModuleContext, path: ModulePath, private var lastMove: Long) :
    AbstractModule(Type, context, path), MoverModule {
    companion object {
        const val MAX_STACK_SIZE = 8
        const val TICKS_PER_MOVE = 40
    }

    private val ignoreStacks = mutableSetOf<ExactStackContainer>()

    override fun move() {
        val now = world.time
        if (now - lastMove >= TICKS_PER_MOVE) {
            lastMove = now

            ignoreStacks.clear()

            val insert = getItemInsertable(frontPos, facing)
            val extractInv = getFixedItemInv(backPos, facing.opposite)

            if (extractInv != EmptyFixedItemInv.INSTANCE) {
                var remaining = MAX_STACK_SIZE
                for (slot in 0 until extractInv.slotCount) {
                    val extract = extractInv.getSlot(slot)
                    remaining = attemptTransfer(remaining, extract, insert, ignoreStacks)
                    if (remaining == 0) break
                }
            } else {
                val extract = getItemExtractable(backPos, facing.opposite)

                attemptTransfer(MAX_STACK_SIZE, extract, insert, ignoreStacks)
            }
        }
    }

    private fun attemptTransfer(
        remaining: Int,
        extract: ItemExtractable,
        insert: ItemInsertable,
        ignore: MutableSet<ExactStackContainer>
    ): Int {
        val extractedSim = extract.attemptAnyExtraction(remaining, Simulation.SIMULATE)
        if (!extractedSim.isEmpty && !ignore.contains(ExactStackContainer.of(extractedSim))) {
            val leftOverSim = insert.attemptInsertion(extractedSim, Simulation.SIMULATE)
            val amount = extractedSim.count - leftOverSim.count

            if (amount != 0) {
                val leftOver =
                    insert.attemptInsertion(
                        extract.attemptAnyExtraction(amount, Simulation.ACTION),
                        Simulation.ACTION
                    )
                assert(leftOver.isEmpty) { "leftOver: $leftOver" }
            } else {
                // If an inventory won't accept a kind of item, don't try and insert it a second time
                ignore.add(ExactStackContainer.of(extractedSim))
            }

            return remaining - amount
        }

        return remaining
    }

    private fun getItemInsertable(pos: BlockPos, direction: Direction): ItemInsertable {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getFixedItemInv(pos: BlockPos, direction: Direction): FixedItemInv {
        return ItemAttributes.FIXED_INV.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getItemExtractable(pos: BlockPos, direction: Direction): ItemExtractable {
        return ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    override fun writeToTag(tag: NbtCompound) {
        tag.putLong("lastMove", lastMove)
    }

    object Type : ModuleType<ItemMoverMk1Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: NbtCompound
        ): ItemMoverMk1Module {
            val lastMove = if (tag.contains("lastMove")) tag.getLong("lastMove") else 0L

            return ItemMoverMk1Module(context, path, lastMove)
        }

        override fun newInstance(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack
        ): ItemMoverMk1Module {
            return ItemMoverMk1Module(context, path, 0L)
        }
    }
}