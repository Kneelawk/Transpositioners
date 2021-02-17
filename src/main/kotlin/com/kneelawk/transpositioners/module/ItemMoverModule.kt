package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ItemMoverModule(entity: TranspositionerEntity, path: ModulePath) :
    AbstractTranspositionerModule(ItemMoverModuleType, entity, path), MoverModule {
    override fun move() {
        val extract = getItemExtractable(
            entity.decorationBlockPos.offset(entity.horizontalFacing.opposite),
            entity.horizontalFacing.opposite
        )
        val insert = getItemInsertable(entity.decorationBlockPos, entity.horizontalFacing)

        val maxAmount = 8

        val extractedSim = extract.attemptAnyExtraction(maxAmount, Simulation.SIMULATE)
        if (!extractedSim.isEmpty) {
            val leftOverSim = insert.attemptInsertion(extractedSim, Simulation.SIMULATE)
            val amount = extractedSim.count - leftOverSim.count

            if (amount != 0) {
                val leftOver =
                    insert.attemptInsertion(
                        extract.attemptAnyExtraction(amount, Simulation.ACTION),
                        Simulation.ACTION
                    )
                assert(leftOver.isEmpty) { "leftOver: $leftOver" }
            }
        }
    }

    private fun getItemInsertable(pos: BlockPos, direction: Direction): ItemInsertable {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getItemExtractable(pos: BlockPos, direction: Direction): ItemExtractable {
        return ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }
}