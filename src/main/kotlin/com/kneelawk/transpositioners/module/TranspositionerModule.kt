package com.kneelawk.transpositioners.module

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.ActiveMinecraftConnection
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

interface TranspositionerModule : ModuleContainer {
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
                return readModulePath(mcConn.player.world, buffer)
            }

            override fun writeContext(buffer: NetByteBuf, ctx: IMsgWriteCtx, value: TranspositionerModule) {
                writeModulePath(value, buffer)
            }
        }

        fun writeModulePath(module: TranspositionerModule, buf: PacketByteBuf) {
            buf.writeInt(module.entity.entityId)
            module.path.writeToBuf(buf)
        }

        fun readModulePath(world: World, buf: PacketByteBuf): TranspositionerModule? {
            val entity = world.getEntityById(buf.readInt()) ?: return null
            val container = entity as? ModuleContainer ?: return null

            val path = ModulePath.readFromBuf(buf)
            if (path.isRoot) {
                LOGGER.warn("Attempting to get a transpositioner module with an empty path")
            }

            return path.findModule(container)
        }
    }

    val path: ModulePath

    val entity: TranspositionerEntity

    val type: ModuleType<*>

    // TODO: Evaluate the usefulness of this.
    fun validate(stack: ItemStack): Boolean

    fun writeToTag(tag: CompoundTag)

    // TODO: Evaluate the usefulness of this.
    fun addStacksForDrop(stacks: MutableCollection<ItemStack>)

    fun onRemove()
}