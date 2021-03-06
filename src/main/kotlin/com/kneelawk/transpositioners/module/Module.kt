package com.kneelawk.transpositioners.module

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.ActiveMinecraftConnection
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

interface Module : ModuleContainer {
    companion object {
        private val LOGGER = LogManager.getLogger()

        val NET_ID = object : ParentNetIdSingle<Module>(
            McNetworkStack.ROOT,
            Module::class.java,
            TPConstants.str("transpositioner_module"),
            -1
        ) {
            override fun readContext(buffer: NetByteBuf, ctx: IMsgReadCtx): Module? {
                val mcConn = ctx.connection as ActiveMinecraftConnection
                return readModulePath(mcConn.player.world, buffer)
            }

            override fun writeContext(buffer: NetByteBuf, ctx: IMsgWriteCtx, value: Module) {
                writeModulePath(value, buffer)
            }
        }

        fun writeModulePath(module: Module, buf: PacketByteBuf) {
            when (val context = module.context) {
                is ModuleContext.Configurator -> {
                    buf.writeBoolean(false)
                    buf.writeBlockPos(context.configurator.pos)
                }
                is ModuleContext.Entity -> {
                    buf.writeBoolean(true)
                    buf.writeInt(context.entity.id)
                }
            }
            module.path.writeToBuf(buf)
        }

        fun readModulePath(world: World, buf: PacketByteBuf): Module? {
            val container = if (buf.readBoolean()) {
                world.getEntityById(buf.readInt()) as? ModuleContainer ?: return null
            } else {
                world.getBlockEntity(buf.readBlockPos()) as? ModuleContainer ?: return null
            }

            val path = ModulePath.readFromBuf(buf)
            if (path.isRoot) {
                LOGGER.warn("Attempting to get a transpositioner module with an empty path")
            }

            return path.findModule(container)
        }
    }

    val path: ModulePath

    val context: ModuleContext

    val type: ModuleType<*>

    // TODO: Evaluate the usefulness of this.
    @Deprecated("This api is unused and likely to be removed.")
    fun validate(stack: ItemStack): Boolean

    fun writeToTag(tag: NbtCompound)

    // TODO: Evaluate the usefulness of this.
    @Deprecated("This api is unused and likely to be removed.")
    fun addStacksForDrop(stacks: MutableCollection<ItemStack>)

    fun onRemove()
}