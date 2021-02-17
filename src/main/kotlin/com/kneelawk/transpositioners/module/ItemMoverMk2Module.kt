package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.screen.ItemMoverMk2ScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ItemMoverMk2Module(entity: TranspositionerEntity, path: ModulePath) :
    AbstractTranspositionerModule(Type, entity, path), MoverModule, ExtendedScreenHandlerFactory {
    companion object {
        const val MAX_STACK_SIZE = 1
    }

    override fun move() {
        val extract = getItemExtractable(attachmentPos.offset(facing.opposite), facing.opposite)
        val insert = getItemInsertable(attachmentPos, facing)

        val extractedSim = extract.attemptAnyExtraction(MAX_STACK_SIZE, Simulation.SIMULATE)
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

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return ItemMoverMk2ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return TranslatableText("item.transpositioners.item_mover_module_mk2")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        TranspositionerModule.writeModulePath(this, buf)
    }

    object Type : ModuleType<ItemMoverMk2Module> {
        override fun readFromTag(
            entity: TranspositionerEntity,
            path: ModulePath,
            stack: ItemStack,
            tag: CompoundTag
        ): ItemMoverMk2Module {
            return ItemMoverMk2Module(entity, path)
        }

        override fun newInstance(
            entity: TranspositionerEntity,
            path: ModulePath,
            stack: ItemStack
        ): ItemMoverMk2Module {
            return ItemMoverMk2Module(entity, path)
        }
    }
}