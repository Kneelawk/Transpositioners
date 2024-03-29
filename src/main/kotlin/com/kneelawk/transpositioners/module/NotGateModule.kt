package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ConstantItemFilter
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.tt
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.screen.NotGateScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World

class NotGateModule(context: ModuleContext, path: ModulePath, val gates: ModuleInventory<ItemGateModule>) :
    AbstractModule(Type, context, path), ItemGateModule, ExtendedScreenHandlerFactory, InventoryChangedListener {

    init {
        gates.addListener(this)
    }

    override fun getItemFilter(): ItemFilter {
        return gates.getModule(0)?.getItemFilter()?.negate() ?: ConstantItemFilter.ANYTHING
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return NotGateScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return tt(TPItems.NOT_GATE_MODULE.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun onInventoryChanged(sender: Inventory) {
        markDirty()
    }

    override fun getModule(index: Int): Module? {
        return gates.getModule(index)
    }

    override fun writeToTag(tag: NbtCompound) {
        tag.put("gates", gates.toNbtList())
    }

    object Type : ModuleType<NotGateModule> {
        override fun readFromTag(
            context: ModuleContext, path: ModulePath, stack: ItemStack, tag: NbtCompound
        ): NotGateModule {
            val gate = ModuleInventory(1, context, path, TPModules.ITEM_GATES)
            if (tag.contains("gates")) {
                gate.readNbtList(tag.getList("gates", 10))
            }

            return NotGateModule(context, path, gate)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): NotGateModule {
            return NotGateModule(context, path, ModuleInventory(1, context, path, TPModules.ITEM_GATES))
        }

        override fun appendTooltip(
            stack: ItemStack, world: World?, tooltip: MutableList<Text>, tooltipContext: TooltipContext,
            moduleData: NbtCompound
        ) {
            if (moduleData.contains("gates")) {
                val gates = moduleData.getList("gates", 10)
                if (!gates.isEmpty()) {
                    tooltip += TPConstants.tooltip("gates", gates.size)
                }
            }
        }
    }
}