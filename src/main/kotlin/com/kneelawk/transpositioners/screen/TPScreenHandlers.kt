package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.TPConstants.identifier
import com.kneelawk.transpositioners.blockentity.ModuleConfiguratorBlockEntity
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.module.*
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.registerExtended
import net.minecraft.screen.ScreenHandlerType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object TPScreenHandlers {
    val MODULE_CONFIGURATOR_ID = identifier("module_configurator")
    val ITEM_GATE_MK1_ID = identifier("item_gate_mk1")
    val ITEM_LOGIC_GATE_ID = identifier("item_logic_gate")
    val ITEM_MOVER_MK2_ID = identifier("item_mover_mk2")
    val ITEM_MOVER_MK3_ID = identifier("item_mover_mk3")
    val ITEM_NOT_GATE_ID = identifier("item_not_gate")
    val TRANSPOSITIONER_ID = identifier("transpositioner")
    val REDSTONE_GATE_ID = identifier("redstone_gate")
    lateinit var MODULE_CONFIGURATOR_TYPE: ScreenHandlerType<ModuleConfiguratorScreenHandler>
    lateinit var ITEM_GATE_MK1_TYPE: ScreenHandlerType<ItemGateMk1ScreenHandler>
    lateinit var ITEM_LOGIC_GATE_TYPE: ScreenHandlerType<ItemLogicGateScreenHandler>
    lateinit var ITEM_MOVER_MK2_TYPE: ScreenHandlerType<ItemMoverMk2ScreenHandler>
    lateinit var ITEM_MOVER_MK3_TYPE: ScreenHandlerType<ItemMoverMk3ScreenHandler>
    lateinit var ITEM_NOT_GATE_TYPE: ScreenHandlerType<ItemNotGateScreenHandler>
    lateinit var TRANSPOSITIONER_TYPE: ScreenHandlerType<TranspositionerScreenHandler>
    lateinit var REDSTONE_GATE_TYPE: ScreenHandlerType<RedstoneGateScreenHandler>

    @OptIn(ExperimentalContracts::class)
    fun register() {
        MODULE_CONFIGURATOR_TYPE = registerExtended(MODULE_CONFIGURATOR_ID) { syncId, playerInventory, buf ->
            val entity = playerInventory.player.world.getBlockEntity(buf.readBlockPos())
            checkType<ModuleConfiguratorBlockEntity>(entity)

            ModuleConfiguratorScreenHandler(syncId, playerInventory, entity)
        }
        ITEM_GATE_MK1_TYPE = registerExtended(ITEM_GATE_MK1_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            checkType<ItemGateMk1Module>(module)

            ItemGateMk1ScreenHandler(syncId, playerInventory, module)
        }
        ITEM_LOGIC_GATE_TYPE = registerExtended(ITEM_LOGIC_GATE_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            checkType<ItemLogicGateModule>(module)

            ItemLogicGateScreenHandler(syncId, playerInventory, module)
        }
        ITEM_MOVER_MK2_TYPE = registerExtended(ITEM_MOVER_MK2_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            checkType<ItemMoverMk2Module>(module)

            ItemMoverMk2ScreenHandler(syncId, playerInventory, module)
        }
        ITEM_MOVER_MK3_TYPE = registerExtended(ITEM_MOVER_MK3_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            checkType<ItemMoverMk3Module>(module)

            ItemMoverMk3ScreenHandler(syncId, playerInventory, module)
        }
        TRANSPOSITIONER_TYPE = registerExtended(TRANSPOSITIONER_ID) { syncId, playerInventory, buf ->
            val entity = playerInventory.player.world.getEntityById(buf.readInt())
            checkType<TranspositionerEntity>(entity)

            TranspositionerScreenHandler(syncId, playerInventory, entity)
        }
        ITEM_NOT_GATE_TYPE = registerExtended(ITEM_NOT_GATE_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            checkType<ItemNotGateModule>(module)

            ItemNotGateScreenHandler(syncId, playerInventory, module)
        }
        REDSTONE_GATE_TYPE = registerExtended(REDSTONE_GATE_ID) { syncId, playerInventory, buf ->
            val module = Module.readModulePath(playerInventory.player.world, buf)
            checkType<RedstoneGateModule>(module)

            RedstoneGateScreenHandler(syncId, playerInventory, module)
        }
    }

    @ExperimentalContracts
    private inline fun <reified T> checkType(obj: Any?) {
        contract {
            returns() implies (obj is T)
        }

        if (obj !is T) {
            badScreen(T::class.simpleName ?: "Unknown")
        }
    }

    private fun badScreen(name: String): Nothing {
        throw IllegalStateException("Received open-$name packet from server but target module was not a $name")
    }
}