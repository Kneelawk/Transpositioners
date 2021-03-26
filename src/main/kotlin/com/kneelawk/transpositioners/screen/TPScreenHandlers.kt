package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.blockentity.ModuleConfiguratorBlockEntity
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.module.ItemGateMk1Module
import com.kneelawk.transpositioners.module.ItemMoverMk2Module
import com.kneelawk.transpositioners.module.Module
import com.kneelawk.transpositioners.module.RedstoneGateModule
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.registerExtended
import net.minecraft.screen.ScreenHandlerType

object TPScreenHandlers {
    val MODULE_CONFIGURATOR_ID = TPConstants.identifier("module_configurator")
    val ITEM_GATE_MK1_ID = TPConstants.identifier("item_gate_mk1")
    val ITEM_MOVER_MK2_ID = TPConstants.identifier("item_mover_mk2")
    val TRANSPOSITIONER_ID = TPConstants.identifier("transpositioner")
    val REDSTONE_GATE_ID = TPConstants.identifier("redstone_gate")
    lateinit var MODULE_CONFIGURATOR_TYPE: ScreenHandlerType<ModuleConfiguratorScreenHandler>
    lateinit var ITEM_GATE_MK1_TYPE: ScreenHandlerType<ItemGateMk1ScreenHandler>
    lateinit var ITEM_MOVER_MK2_TYPE: ScreenHandlerType<ItemMoverMk2ScreenHandler>
    lateinit var TRANSPOSITIONER_TYPE: ScreenHandlerType<TranspositionerScreenHandler>
    lateinit var REDSTONE_GATE_TYPE: ScreenHandlerType<RedstoneGateScreenHandler>

    fun register() {
        MODULE_CONFIGURATOR_TYPE = registerExtended(MODULE_CONFIGURATOR_ID) { syncId, playerInventory, buf ->
            val entity = playerInventory.player.world.getBlockEntity(buf.readBlockPos())
            if (entity !is ModuleConfiguratorBlockEntity) {
                throw IllegalStateException(
                    "Received open-ModuleConfigurator packet from server but target block-entity was not a ModuleConfigurator"
                )
            }

            ModuleConfiguratorScreenHandler(syncId, playerInventory, entity)
        }
        ITEM_GATE_MK1_TYPE = registerExtended(ITEM_GATE_MK1_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            if (module !is ItemGateMk1Module) {
                throw IllegalStateException(
                    "Received open-ItemGateMk1Module packet from server but target module was not an ItemGateMk1Module"
                )
            }

            ItemGateMk1ScreenHandler(syncId, playerInventory, module)
        }
        ITEM_MOVER_MK2_TYPE = registerExtended(ITEM_MOVER_MK2_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            if (module !is ItemMoverMk2Module) {
                throw IllegalStateException(
                    "Received open-ItemMoverMk2Module packet from server but target module was not an ItemMoverMk2Module"
                )
            }

            ItemMoverMk2ScreenHandler(syncId, playerInventory, module)
        }
        TRANSPOSITIONER_TYPE = registerExtended(TRANSPOSITIONER_ID) { syncId, playerInventory, buf ->
            val entity = playerInventory.player.world.getEntityById(buf.readInt())
            if (entity !is TranspositionerEntity) {
                throw IllegalStateException(
                    "Received open-transpositioner-inventory packet from server but target entity was not a transpositioner"
                )
            }

            TranspositionerScreenHandler(syncId, playerInventory, entity)
        }
        REDSTONE_GATE_TYPE = registerExtended(REDSTONE_GATE_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            if (module !is RedstoneGateModule) {
                throw IllegalStateException(
                    "Received open-RedstoneGateModule packet from server but target module was not a RedstoneGateModule"
                )
            }

            RedstoneGateScreenHandler(syncId, playerInventory, module)
        }
    }
}