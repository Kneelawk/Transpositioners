package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.screen.ItemFilterMk1ScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ItemFilterMk1Module(context: ModuleContext, path: ModulePath) : AbstractModule(Type, context, path),
    ItemFilterModule, ExtendedScreenHandlerFactory {
    override val itemFilter = ItemFilter { true }

    override fun shouldMove(): Boolean {
        return true
    }

    override fun writeToTag(tag: CompoundTag) {
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler? {
        return ItemFilterMk1ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return TPConstants.item("item_filter_module_mk1")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    object Type : ModuleType<ItemFilterMk1Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: CompoundTag
        ): ItemFilterMk1Module {
            return ItemFilterMk1Module(context, path)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): ItemFilterMk1Module {
            return ItemFilterMk1Module(context, path)
        }
    }
}