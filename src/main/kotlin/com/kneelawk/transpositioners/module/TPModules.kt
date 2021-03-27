package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.item.TPItems
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.world.World

object TPModules {
    private const val MODULE_TAG_NAME = "module"

    // TODO: Figure out a better way to do this.
    val GLOBAL = ModuleRegistry<Module>()
    val MOVERS = ModuleRegistry<MoverModule>()
    val ITEM_GATES = ModuleRegistry<ItemGateModule>()

    fun register() {
        register(ItemGateMk1Module.Type, TPItems.ITEM_GATE_MODULE_MK1)
        register(ItemLogicGateModule.Type, TPItems.ITEM_AND_GATE_MODULE_MK1)
        register(ItemLogicGateModule.Type, TPItems.ITEM_AND_GATE_MODULE_MK2)
        register(ItemLogicGateModule.Type, TPItems.ITEM_AND_GATE_MODULE_MK3)
        register(ItemLogicGateModule.Type, TPItems.ITEM_OR_GATE_MODULE_MK1)
        register(ItemLogicGateModule.Type, TPItems.ITEM_OR_GATE_MODULE_MK2)
        register(ItemLogicGateModule.Type, TPItems.ITEM_OR_GATE_MODULE_MK3)
        register(ItemMoverMk1Module.Type, TPItems.ITEM_MOVER_MODULE_MK1)
        register(ItemMoverMk2Module.Type, TPItems.ITEM_MOVER_MODULE_MK2)
        register(ItemMoverMk3Module.Type, TPItems.ITEM_MOVER_MODULE_MK3)
        register(ItemNotGateModule.Type, TPItems.ITEM_NOT_GATE_MODULE)
        register(RedstoneGateModule.Type, TPItems.REDSTONE_GATE_MODULE)
    }

    // kotlin interface
    inline fun <reified M : Module> register(type: ModuleType<M>, item: Item) {
        register(M::class.java, type, item)
    }

    // java interface
    fun <M : Module> register(clazz: Class<M>, type: ModuleType<M>, item: Item) {
        if (GLOBAL.containsItem(item)) {
            throw IllegalStateException("A module type is already registered for the item: $item")
        }
        GLOBAL.register(type, item)

        if (MoverModule::class.java.isAssignableFrom(clazz)) {
            MOVERS.register(type as ModuleType<out MoverModule>, item)
        }

        if (ItemGateModule::class.java.isAssignableFrom(clazz)) {
            ITEM_GATES.register(type as ModuleType<out ItemGateModule>, item)
        }
    }

    fun getModuleData(stack: ItemStack): CompoundTag? {
        return stack.getSubTag(TPConstants.str(MODULE_TAG_NAME))
    }

    fun <M : Module> getModule(
        stack: ItemStack,
        type: ModuleType<out M>,
        context: ModuleContext,
        path: ModulePath
    ): M {
        val holder = getModuleData(stack)
        return if (holder == null) {
            type.newInstance(context, path, stack)
        } else {
            type.readFromTag(context, path, stack, holder)
        }
    }

    fun putModuleData(stack: ItemStack, data: CompoundTag) {
        stack.putSubTag(TPConstants.str(MODULE_TAG_NAME), data)
    }

    fun putModule(stack: ItemStack, module: Module) {
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
        val moduleData = getModuleData(stack)
        if (moduleData != null) {
            GLOBAL.maybeGetByItem(stack.item)?.appendTooltip(stack, world, tooltip, context, moduleData)
        } else {
            tooltip.add(TPConstants.tooltip("unconfigured"))
        }
    }
}