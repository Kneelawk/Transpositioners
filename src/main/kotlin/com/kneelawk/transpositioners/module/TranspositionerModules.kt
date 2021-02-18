package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.item.TranspositionerItems
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.world.World

object TranspositionerModules {
    private const val MODULE_TAG_NAME = "module"

    // this is a tad janky but it'll work well enough for now
    internal val globalItemMap = mutableMapOf<Item, ModuleType<*>>()

    val MOVERS = ModuleRegistry<MoverModule>()

    fun register() {
        MOVERS.register(ItemMoverMk1Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK1)
        MOVERS.register(ItemMoverMk2Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK2)
        MOVERS.register(ItemMoverMk3Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK3)
    }

    fun getGlobalByItem(item: Item): ModuleType<*>? {
        return globalItemMap[item]
    }

    fun getModuleData(stack: ItemStack): CompoundTag? {
        return stack.getSubTag(TranspositionersConstants.str(MODULE_TAG_NAME))
    }

    fun <M : TranspositionerModule> getModule(
        stack: ItemStack,
        type: ModuleType<out M>,
        entity: TranspositionerEntity,
        path: ModulePath
    ): M {
        val holder = getModuleData(stack)
        return if (holder == null) {
            type.newInstance(entity, path, stack)
        } else {
            type.readFromTag(entity, path, stack, holder)
        }
    }

    fun putModuleData(stack: ItemStack, data: CompoundTag) {
        stack.putSubTag(TranspositionersConstants.str(MODULE_TAG_NAME), data)
    }

    fun putModule(stack: ItemStack, module: TranspositionerModule) {
        val holder = CompoundTag()
        module.writeToTag(holder)
        putModuleData(stack, holder)
    }

    fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        getModuleData(stack)?.let { moduleData ->
            getGlobalByItem(stack.item)?.appendTooltip(stack, world, tooltip, context, moduleData)
        }
    }
}