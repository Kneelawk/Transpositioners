package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType

object TranspositionerScreenHandlers {
    val TRANSPOSITIONER_ID = TranspositionersConstants.identifier("transpositioner")
    lateinit var TRANSPOSITIONER_TYPE: ScreenHandlerType<TranspositionerScreenHandler>

    fun register() {
        TRANSPOSITIONER_TYPE =
            ScreenHandlerRegistry.registerExtended(TRANSPOSITIONER_ID) { syncId, playerInventory, buf ->
                val entity = playerInventory.player.world.getEntityById(buf.readInt())
                if (entity !is TranspositionerEntity) {
                    throw IllegalStateException("Received open-transpositioner-inventory packet from server but target entity was not a transpositioner")
                }

                TranspositionerScreenHandler(syncId, playerInventory, entity, ScreenHandlerContext.EMPTY)
            }
    }
}