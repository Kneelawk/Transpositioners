package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.blockentity.ModuleConfiguratorBlockEntity
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.module.ItemFilterMk1Module
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.module.Module
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.screen.ScreenHandlerType

object TPScreenHandlers {
    val MODULE_CONFIGURATOR_ID = TPConstants.identifier("module_configurator")
    val ITEM_FILTER_MK1_ID = TPConstants.identifier("item_filter_mk1")
    val ITEM_MOVER_MK2_ID = TPConstants.identifier("item_mover_mk2")
    val TRANSPOSITIONER_ID = TPConstants.identifier("transpositioner")
    lateinit var MODULE_CONFIGURATOR_TYPE: ScreenHandlerType<ModuleConfiguratorScreenHandler>
    lateinit var ITEM_FILTER_MK1_TYPE: ScreenHandlerType<ItemFilterMk1ScreenHandler>
    lateinit var ITEM_MOVER_MK2_TYPE: ScreenHandlerType<ItemMoverMk2ScreenHandler>
    lateinit var TRANSPOSITIONER_TYPE: ScreenHandlerType<TranspositionerScreenHandler>

    fun register() {
        MODULE_CONFIGURATOR_TYPE =
            ScreenHandlerRegistry.registerExtended(MODULE_CONFIGURATOR_ID) { syncId, playerInventory, buf ->
                val entity = playerInventory.player.world.getBlockEntity(buf.readBlockPos())
                if (entity !is ModuleConfiguratorBlockEntity) {
                    throw IllegalStateException("Received open-ModuleConfigurator packet from server but target block-entity was not a ModuleConfigurator")
                }

                ModuleConfiguratorScreenHandler(syncId, playerInventory, entity)
            }
        ITEM_FILTER_MK1_TYPE =
            ScreenHandlerRegistry.registerExtended(ITEM_FILTER_MK1_ID) { syncId, playerInventory, buf ->
                val module = Module.readModulePath(playerInventory.player.world, buf)
                if (module !is ItemFilterMk1Module) {
                    throw IllegalStateException("Received open-ItemFilterMk1Module packet from server but target module was not an ItemFilterMk1Module")
                }

                ItemFilterMk1ScreenHandler(syncId, playerInventory, module)
            }
        ITEM_MOVER_MK2_TYPE =
            ScreenHandlerRegistry.registerExtended(ITEM_MOVER_MK2_ID) { syncId, playerInventory, buf ->
                val module = Module.readModulePath(playerInventory.player.world, buf)
                if (module !is ItemMoverMk2Module) {
                    throw IllegalStateException("Received open-ItemMoverMk2Module packet from server but target module was not an ItemMoverMk2Module")
                }

                ItemMoverMk2ScreenHandler(syncId, playerInventory, module)
            }
        TRANSPOSITIONER_TYPE =
            ScreenHandlerRegistry.registerExtended(TRANSPOSITIONER_ID) { syncId, playerInventory, buf ->
                val entity = playerInventory.player.world.getEntityById(buf.readInt())
                if (entity !is TranspositionerEntity) {
                    throw IllegalStateException("Received open-transpositioner-inventory packet from server but target entity was not a transpositioner")
                }

                TranspositionerScreenHandler(syncId, playerInventory, entity)
            }
    }
}