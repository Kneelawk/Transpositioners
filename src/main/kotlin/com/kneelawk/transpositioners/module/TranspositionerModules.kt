package com.kneelawk.transpositioners.module

import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.item.TranspositionerItems
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

object TranspositionerModules {
    private const val MODULE_TAG_NAME = "module"

    // TODO: Figure out a better way to do this.
    val GLOBAL = ModuleRegistry<TranspositionerModule>()
    val MOVERS = ModuleRegistry<MoverModule>()

    fun register() {
        register(ItemMoverMk1Module::class, ItemMoverMk1Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK1)
        register(ItemMoverMk2Module::class, ItemMoverMk2Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK2)
        register(ItemMoverMk3Module::class, ItemMoverMk3Module.Type, TranspositionerItems.ITEM_MOVER_MODULE_MK3)
    }

    fun <M : TranspositionerModule> register(clazz: KClass<M>, type: ModuleType<M>, item: Item) {
        if (GLOBAL.containsItem(item)) {
            throw IllegalStateException("A module type is already registered for the item: $item")
        }
        GLOBAL.register(type, item)

        if (MoverModule::class.isSuperclassOf(clazz)) {
            MOVERS.register(type as ModuleType<out MoverModule>, item)
        }
    }

    fun getModuleData(stack: ItemStack): CompoundTag? {
        return stack.getSubTag(TranspositionersConstants.str(MODULE_TAG_NAME))
    }

    fun <M : TranspositionerModule> getModule(
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
            GLOBAL.maybeGetByItem(stack.item)?.appendTooltip(stack, world, tooltip, context, moduleData)
        }
    }
}