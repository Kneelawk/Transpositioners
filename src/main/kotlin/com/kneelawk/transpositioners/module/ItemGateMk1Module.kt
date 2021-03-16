package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.screen.ItemGateMk1ScreenHandler
import com.kneelawk.transpositioners.util.BasicStackContainer
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World

class ItemGateMk1Module(context: ModuleContext, path: ModulePath, val items: TPSimpleInventory) :
    AbstractModule(Type, context, path),
    ItemGateModule, ExtendedScreenHandlerFactory {
    override fun getItemFilter(): ItemFilter {
        val itemSet = hashSetOf<BasicStackContainer>()
        for (i in 0 until items.size()) {
            val item = items.getStack(i)
            if (!item.isEmpty) {
                itemSet.add(BasicStackContainer.of(item))
            }
        }

        return ItemFilter {
            itemSet.contains(BasicStackContainer.of(it))
        }
    }

    override fun shouldMove(): Boolean {
        return true
    }

    override fun writeToTag(tag: CompoundTag) {
        tag.put("items", items.tags)
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
            val items = TPSimpleInventory(9)

            if (tag.contains("items")) {
                items.readTags(tag.getList("items", 10))
            }

            return ItemGateMk1Module(context, path, items)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): ItemGateMk1Module {
            return ItemGateMk1Module(context, path, TPSimpleInventory(9))
        }

        override fun appendTooltip(
            stack: ItemStack, world: World?, tooltip: MutableList<Text>,
            tooltipContext: TooltipContext, moduleData: CompoundTag
        ) {
            if (moduleData.contains("items")) {
                val gates = moduleData.getList("items", 10)
                if (!gates.isEmpty()) {
                    tooltip += TPConstants.tooltip("items", gates.size)
                }
            }
        }
    }
}