package com.kneelawk.transpositioners.blockentity

import com.kneelawk.transpositioners.block.TranspositionerBlocks
import com.kneelawk.transpositioners.module.*
import com.kneelawk.transpositioners.screen.ModuleConfiguratorScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

class ModuleConfiguratorBlockEntity : BlockEntity(TranspositionerBlockEntities.MODULE_CONFIGURATOR),
    ExtendedScreenHandlerFactory, ModuleContainer {
    companion object {
        private const val MODULE_INVENTORY_TAG = "Inventory"
    }

    val modules = ModuleInventory(
        1,
        ModuleContext.Configurator(this),
        ModulePath.ROOT,
        TranspositionerModules.GLOBAL
    )

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)

        modules.readTags(tag.getList(MODULE_INVENTORY_TAG, 10))
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        val newTag = super.toTag(tag)

        newTag.put(MODULE_INVENTORY_TAG, modules.getTags())

        return newTag
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return ModuleConfiguratorScreenHandler(syncId, player.inventory, this)
    }

    override fun getDisplayName(): Text {
        return TranslatableText(TranspositionerBlocks.MODULE_CONFIGURATOR.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun getModule(index: Int): TranspositionerModule? {
        return modules.getModule(index)
    }
}