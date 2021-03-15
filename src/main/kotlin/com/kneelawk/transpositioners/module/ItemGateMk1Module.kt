package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.screen.ItemGateMk1ScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ItemGateMk1Module(context: ModuleContext, path: ModulePath) : AbstractModule(Type, context, path),
    ItemGateModule, ExtendedScreenHandlerFactory {
    override val itemFilter = ItemFilter { true }

    override fun shouldMove(): Boolean {
        return true
    }

    override fun writeToTag(tag: CompoundTag) {
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler? {
        return ItemGateMk1ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return TPItems.ITEM_GATE_MODULE_MK1.name
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    object Type : ModuleType<ItemGateMk1Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: CompoundTag
        ): ItemGateMk1Module {
            return ItemGateMk1Module(context, path)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): ItemGateMk1Module {
            return ItemGateMk1Module(context, path)
        }
    }
}