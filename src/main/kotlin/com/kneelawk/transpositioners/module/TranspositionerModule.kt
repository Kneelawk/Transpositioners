package com.kneelawk.transpositioners.module

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.ActiveMinecraftConnection
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TranspositionersConstants
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import org.apache.logging.log4j.LogManager

interface TranspositionerModule : ModuleContainer, ExtendedScreenHandlerFactory {
    companion object {
        private val LOGGER = LogManager.getLogger()

        val NET_ID = object : ParentNetIdSingle<TranspositionerModule>(
            McNetworkStack.ROOT,
            TranspositionerModule::class.java,
            TranspositionersConstants.str("transpositioner_module"),
            -1
        ) {
            override fun readContext(buffer: NetByteBuf, ctx: IMsgReadCtx): TranspositionerModule? {
                val mcConn = ctx.connection as ActiveMinecraftConnection
                val player = mcConn.player
                val entity = player.world.getEntityById(buffer.readInt()) ?: return null
                val container = entity as? ModuleContainer ?: return null

                val path = ModulePath.readFromBuf(buffer)
                if (path.isRoot) {
                    LOGGER.warn("Attempting to get a transpositioner module with an empty path")
                }

                return path.findModule(container)
            }

            override fun writeContext(buffer: NetByteBuf, ctx: IMsgWriteCtx, value: TranspositionerModule) {
                buffer.writeInt(value.entity.entityId)
                value.path.writeToBuf(buffer)
            }
        }
    }

    val path: ModulePath

    val entity: Entity

    val type: ModuleType<*>

    fun validate(stack: ItemStack): ValidationResult

    fun writeToTag(tag: CompoundTag)

    fun addStacksForDrop(stacks: MutableCollection<ItemStack>)

    fun onRemove(type: RemovalType)

    enum class ValidationResult {
        VALID,
        DROP,
        DELETE
    }

    enum class RemovalType {
        DELETE,
        DROP
    }
}