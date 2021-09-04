package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ExactItemSetFilter
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.TPConstants.tt
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.net.ModuleDataPacketHandler
import com.kneelawk.transpositioners.screen.ItemGateMk1ScreenHandler
import com.kneelawk.transpositioners.util.ListGateType
import com.kneelawk.transpositioners.util.TooltipUtils.listGateType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World

class ItemGateMk1Module(
    context: ModuleContext, path: ModulePath, val items: TPSimpleInventory, initialGateType: ListGateType
) :
    AbstractModule(Type, context, path),
    ItemGateModule, ExtendedScreenHandlerFactory, InventoryChangedListener {

    companion object {
        private val NET_PARENT = Module.NET_ID.subType(ItemGateMk1Module::class.java, str("item_gate_mk1_module"))

        private val GATE_TYPE_CHANGE = ModuleDataPacketHandler(NET_PARENT.idData("GATE_TYPE_CHANGE"),
            ItemGateMk1ScreenHandler::class, { it.writeByte(gateType.id) },
            { gateType = ListGateType.byId(it.readByte().toInt()) },
            { it.s2cReceiveGateTypeChange(gateType) })
    }

    var gateType = initialGateType
        private set

    init {
        items.addListener(this)
    }

    fun updateGateType(type: ListGateType) {
        gateType = type
        GATE_TYPE_CHANGE.sendToClients(this)
        markDirty()
    }

    override fun getItemFilter(): ItemFilter {
        val itemSet = hashSetOf<Item>()
        for (i in 0 until items.size()) {
            val item = items.getStack(i)
            if (!item.isEmpty) {
                itemSet.add(item.item)
            }
        }

        val filter = ExactItemSetFilter(itemSet)

        return if (gateType == ListGateType.ALLOW) {
            filter
        } else {
            filter.negate()
        }
    }

    override fun writeToTag(tag: NbtCompound) {
        tag.put("items", items.toNbtList())
        tag.putByte("gate_type", gateType.id.toByte())
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler? {
        return ItemGateMk1ScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return tt(TPItems.ITEM_GATE_MODULE_MK1.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun onInventoryChanged(sender: Inventory) {
        markDirty()
    }

    object Type : ModuleType<ItemGateMk1Module> {
        override fun readFromTag(
            context: ModuleContext,
            path: ModulePath,
            stack: ItemStack,
            tag: NbtCompound
        ): ItemGateMk1Module {
            val items = TPSimpleInventory(9)

            if (tag.contains("items")) {
                items.readNbtList(tag.getList("items", 10))
            }

            val gateType = if (tag.contains("gate_type")) {
                ListGateType.byId(tag.getByte("gate_type").toInt())
            } else {
                ListGateType.ALLOW
            }

            return ItemGateMk1Module(context, path, items, gateType)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): ItemGateMk1Module {
            return ItemGateMk1Module(context, path, TPSimpleInventory(9), ListGateType.ALLOW)
        }

        override fun appendTooltip(
            stack: ItemStack, world: World?, tooltip: MutableList<Text>,
            tooltipContext: TooltipContext, moduleData: NbtCompound
        ) {
            if (moduleData.contains("items")) {
                val gates = moduleData.getList("items", 10)
                if (!gates.isEmpty()) {
                    tooltip += TPConstants.tooltip("items", gates.size)
                }
            }

            if (moduleData.contains("gate_type")) {
                tooltip += listGateType(ListGateType.byId(moduleData.getByte("gate_type").toInt()))
            }
        }
    }
}