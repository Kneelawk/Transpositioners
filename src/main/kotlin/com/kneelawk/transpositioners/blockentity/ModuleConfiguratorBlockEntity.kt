package com.kneelawk.transpositioners.blockentity

import com.kneelawk.transpositioners.block.TPBlocks
import com.kneelawk.transpositioners.module.*
import com.kneelawk.transpositioners.screen.ModuleConfiguratorScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos

class ModuleConfiguratorBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(TPBlockEntities.MODULE_CONFIGURATOR, pos, state),
    ExtendedScreenHandlerFactory, ModuleContainer, InventoryChangedListener {
    companion object {
        private const val MODULE_INVENTORY_TAG = "Inventory"
    }

    val modules = ModuleInventory(
        1,
        ModuleContext.Configurator(this),
        ModulePath.ROOT,
        TPModules.GLOBAL
    )

    init {
        modules.addListener(this)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)

        modules.readNbtList(tag.getList(MODULE_INVENTORY_TAG, 10))
    }

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)

        tag.put(MODULE_INVENTORY_TAG, modules.toNbtList())
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return ModuleConfiguratorScreenHandler(syncId, player.inventory, this)
    }

    override fun getDisplayName(): Text {
        return TranslatableText(TPBlocks.MODULE_CONFIGURATOR.translationKey)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun getModule(index: Int): Module? {
        return modules.getModule(index)
    }

    override fun onInventoryChanged(sender: Inventory) {
        markDirty()
    }
}