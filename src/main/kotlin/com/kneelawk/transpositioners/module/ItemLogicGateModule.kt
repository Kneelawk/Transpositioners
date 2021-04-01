package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.AggregateFilterType
import alexiil.mc.lib.attributes.item.filter.AggregateItemFilter
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.TPConstants.tt
import com.kneelawk.transpositioners.item.TPItems
import com.kneelawk.transpositioners.net.ModuleDataPacketHandler
import com.kneelawk.transpositioners.screen.ItemLogicGateScreenHandler
import com.kneelawk.transpositioners.util.TooltipUtils
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.streams.toList

class ItemLogicGateModule(
    context: ModuleContext, path: ModulePath, val mk: Int, val andGate: Boolean,
    val gates: ModuleInventory<ItemGateModule>, initialNotState: Boolean
) : AbstractModule(Type, context, path), ItemGateModule, ExtendedScreenHandlerFactory {
    companion object {
        const val MIN_MK = 1
        const val MAX_MK = 3

        private val NET_PARENT = Module.NET_ID.subType(ItemLogicGateModule::class.java, str("item_logic_gate_module"))

        private val CHANGE_NOT_STATE = ModuleDataPacketHandler(NET_PARENT.idData("CHANGE_NOT_STATE"),
            ItemLogicGateScreenHandler::class, { it.writeBoolean(notState) },
            { notState = it.readBoolean() }, { it.s2cChangeNotState(notState) })

        fun gateCountByMk(mk: Int): Int {
            return when (mk) {
                1    -> 2
                2    -> 4
                3    -> 16
                else -> throw IllegalArgumentException("Unknown LogicGate mk: $mk")
            }
        }

        fun isAndGate(item: Item) = when (item) {
            TPItems.ITEM_AND_GATE_MODULE_MK1 -> true
            TPItems.ITEM_AND_GATE_MODULE_MK2 -> true
            TPItems.ITEM_AND_GATE_MODULE_MK3 -> true
            TPItems.ITEM_OR_GATE_MODULE_MK1  -> false
            TPItems.ITEM_OR_GATE_MODULE_MK2  -> false
            TPItems.ITEM_OR_GATE_MODULE_MK3  -> false
            else                             -> throw IllegalArgumentException(
                "Unknown LogicGate item: ${item.translationKey}"
            )
        }

        fun getMk(item: Item) = when (item) {
            TPItems.ITEM_AND_GATE_MODULE_MK1 -> 1
            TPItems.ITEM_AND_GATE_MODULE_MK2 -> 2
            TPItems.ITEM_AND_GATE_MODULE_MK3 -> 3
            TPItems.ITEM_OR_GATE_MODULE_MK1  -> 1
            TPItems.ITEM_OR_GATE_MODULE_MK2  -> 2
            TPItems.ITEM_OR_GATE_MODULE_MK3  -> 3
            else                             -> throw IllegalArgumentException(
                "Unknown LogicGate item: ${item.translationKey}"
            )
        }

        fun getItem(mk: Int, andGate: Boolean) = if (andGate) {
            when (mk) {
                1    -> TPItems.ITEM_AND_GATE_MODULE_MK1
                2    -> TPItems.ITEM_AND_GATE_MODULE_MK2
                3    -> TPItems.ITEM_AND_GATE_MODULE_MK3
                else -> throw IllegalStateException("Unknown AND LogicGate item with mk: $mk")
            }
        } else {
            when (mk) {
                1    -> TPItems.ITEM_OR_GATE_MODULE_MK1
                2    -> TPItems.ITEM_OR_GATE_MODULE_MK2
                3    -> TPItems.ITEM_OR_GATE_MODULE_MK3
                else -> throw IllegalStateException("Unknown OR LogicGate item with mk: $mk")
            }
        }
    }

    var notState = initialNotState
        private set

    fun updateNotState(notState: Boolean) {
        this.notState = notState
        CHANGE_NOT_STATE.sendToClients(this)
    }

    override fun getItemFilter(): ItemFilter {
        val filter = AggregateItemFilter.combine(
            if (andGate) AggregateFilterType.ALL else AggregateFilterType.ANY,
            gates.moduleStream().filter { it != null }.map { it!!.getItemFilter() }.toList()
        )

        return if (notState) {
            filter.negate()
        } else {
            filter
        }
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return ItemLogicGateScreenHandler(syncId, inv, this)
    }

    override fun getDisplayName(): Text {
        return tt(getItem(mk, andGate).translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        Module.writeModulePath(this, buf)
    }

    override fun getModule(index: Int): Module? {
        return gates.getModule(index)
    }

    override fun writeToTag(tag: CompoundTag) {
        tag.put("gates", gates.getTags())
        if (mk > 1) tag.putBoolean("not_state", notState)
    }

    object Type : ModuleType<ItemLogicGateModule> {
        override fun readFromTag(
            context: ModuleContext, path: ModulePath, stack: ItemStack, tag: CompoundTag
        ): ItemLogicGateModule {
            val item = stack.item
            val mk = getMk(item)
            val andGate = isAndGate(item)

            val gates = ModuleInventory(gateCountByMk(mk), context, path, TPModules.ITEM_GATES)
            if (tag.contains("gates")) {
                gates.readTags(tag.getList("gates", 10))
            }

            val notState = if (mk > 1 && tag.contains("not_state")) {
                tag.getBoolean("not_state")
            } else {
                false
            }

            return ItemLogicGateModule(context, path, mk, andGate, gates, notState)
        }

        override fun newInstance(context: ModuleContext, path: ModulePath, stack: ItemStack): ItemLogicGateModule {
            val item = stack.item
            val mk = getMk(item)
            return ItemLogicGateModule(
                context, path, mk, isAndGate(item),
                ModuleInventory(gateCountByMk(mk), context, path, TPModules.ITEM_GATES), false
            )
        }

        override fun appendTooltip(
            stack: ItemStack, world: World?, tooltip: MutableList<Text>, tooltipContext: TooltipContext,
            moduleData: CompoundTag
        ) {
            if (moduleData.contains("gates")) {
                val gates = moduleData.getList("gates", 10)
                if (!gates.isEmpty()) {
                    tooltip += TPConstants.tooltip("gates", gates.size)
                }
            }

            if (moduleData.contains("not_state")) {
                tooltip += TooltipUtils.notState(moduleData.getBoolean("not_state"))
            }
        }
    }
}