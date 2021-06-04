package com.kneelawk.transpositioners.entity

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import alexiil.mc.lib.attributes.item.impl.EmptyItemExtractable
import alexiil.mc.lib.attributes.item.impl.RejectingItemInsertable
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.tt
import com.kneelawk.transpositioners.item.TranspositionerItem
import com.kneelawk.transpositioners.module.*
import com.kneelawk.transpositioners.screen.TranspositionerScreenHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.*
import net.minecraft.entity.decoration.AbstractDecorationEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.GameRules
import net.minecraft.world.World
import org.apache.commons.lang3.Validate

class TranspositionerEntity : AbstractDecorationEntity, ExtendedScreenHandlerFactory, ModuleContainer {
    companion object {
        private val NET_PARENT: ParentNetIdSingle<TranspositionerEntity> = McNetworkStack.ENTITY.subType(
            TranspositionerEntity::class.java,
            TPConstants.str("transpositioner_entity")
        )
        private val ID_CHANGE_MK: NetIdDataK<TranspositionerEntity> =
            NET_PARENT.idData("CHANGE_MK").setReceiver(TranspositionerEntity::receiveMkChange)
        private val ID_INSERT_MODULE: NetIdDataK<TranspositionerEntity> =
            NET_PARENT.idData("INSERT_MODULE").setReceiver(TranspositionerEntity::receiveInsertModule)

        const val MIN_MK = 1
        const val MAX_MK = 3

        fun moduleCountByMk(mk: Int): Int {
            return when (mk) {
                1 -> 1
                2 -> 4
                3 -> 16
                else -> throw IllegalArgumentException("Unknown transpositioner mk $mk")
            }
        }
    }

    var mk: Int
        private set
    var modules: ModuleInventory<MoverModule>

    constructor(entityType: EntityType<out TranspositionerEntity>, world: World) : super(entityType, world) {
        mk = MIN_MK
        modules = ModuleInventory(
            moduleCountByMk(mk),
            ModuleContext.Entity(this),
            ModulePath.ROOT,
            TPModules.MOVERS
        )
    }

    constructor(world: World, pos: BlockPos, direction: Direction, mk: Int) : super(
        TPEntityTypes.TRANSPOSITIONER,
        world,
        pos
    ) {
        setFacing(direction)
        this.mk = mk.coerceIn(MIN_MK, MAX_MK)
        modules = ModuleInventory(
            moduleCountByMk(mk),
            ModuleContext.Entity(this),
            ModulePath.ROOT,
            TPModules.MOVERS
        )
    }

    override fun setFacing(facing: Direction) {
        Validate.notNull(facing)
        this.facing = facing
        if (facing.axis.isHorizontal) {
            pitch = 0.0f
            yaw = (facing.horizontal * 90).toFloat()
        } else {
            pitch = (-90 * facing.direction.offset()).toFloat()
            yaw = 0.0f
        }
        prevPitch = pitch
        prevYaw = yaw
        updateAttachmentPosition()
    }

    private fun setModuleCount(count: Int, dropItems: Boolean) {
        val conversion = modules.convertToNewSized(count)
        modules = conversion.newInventory

        if (dropItems) {
            for (stack in conversion.remainingStacks) {
                dropStack(stack)
            }
        }
    }

    fun updateMk(mk: Int) {
        if (!world.isClient) {
            this.mk = mk.coerceIn(MIN_MK, MAX_MK)
            sendMkChage(this.mk)
            setModuleCount(moduleCountByMk(mk), true)
        }
    }

    private fun sendMkChage(mk: Int) {
        for (con in CoreMinecraftNetUtil.getPlayersWatching(world, attachmentPos)) {
            ID_CHANGE_MK.send(con, this) { _, buf, ctx ->
                ctx.assertServerSide()
                buf.writeByte(mk)
            }
        }
    }

    private fun receiveMkChange(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertClientSide()
        mk = buf.readByte().toInt().coerceIn(MIN_MK, MAX_MK)
        setModuleCount(moduleCountByMk(mk), false)
    }

    fun canInsertModule(stack: ItemStack): Boolean {
        return modules.canInsert(stack)
    }

    fun insertModule(stack: ItemStack) {
        if (!world.isClient && modules.canInsert(stack)) {
            val toInsert = stack.split(1)
            sendInsertModule(toInsert)
            modules.addStack(toInsert)
        }
    }

    private fun sendInsertModule(toInsert: ItemStack) {
        for (con in CoreMinecraftNetUtil.getPlayersWatching(world, attachmentPos)) {
            ID_INSERT_MODULE.send(con, this) { _, buf, ctx ->
                ctx.assertServerSide()
                buf.writeItemStack(toInsert)
            }
        }
    }

    private fun receiveInsertModule(buf: PacketByteBuf, ctx: IMsgReadCtx) {
        ctx.assertClientSide()
        modules.addStack(buf.readItemStack())
    }

    override fun updateAttachmentPosition() {
        if (facing != null) {
            val e = attachmentPos.x.toDouble() + 0.5 - facing.offsetX.toDouble() * 8.0 / 16.0
            val f = attachmentPos.y.toDouble() + 0.5 - facing.offsetY.toDouble() * 8.0 / 16.0
            val g = attachmentPos.z.toDouble() + 0.5 - facing.offsetZ.toDouble() * 8.0 / 16.0
            setPos(e, f, g)
            var h = this.widthPixels.toDouble()
            var i = this.heightPixels.toDouble()
            var j = this.widthPixels.toDouble()
            when (facing.axis!!) {
                Direction.Axis.X -> h = 2.0
                Direction.Axis.Y -> i = 2.0
                Direction.Axis.Z -> j = 2.0
            }
            h /= 32.0
            i /= 32.0
            j /= 32.0
            this.boundingBox = Box(e - h, f - i, g - j, e + h, f + i, g + j)
        }
    }

    override fun canStayAttached(): Boolean {
        return (isValid(attachmentPos, facing) || isValid(attachmentPos.offset(facing.opposite), facing.opposite)) &&
                world.getOtherEntities(this, boundingBox, PREDICATE).isEmpty()
    }

    private fun isValid(pos: BlockPos, direction: Direction): Boolean {
        val insertable = getItemInsertable(pos, direction)
        val extractable = getItemExtractable(pos, direction)
        return insertable !is RejectingItemInsertable || extractable !is EmptyItemExtractable
    }

    private fun getItemInsertable(pos: BlockPos, direction: Direction): ItemInsertable {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    private fun getItemExtractable(pos: BlockPos, direction: Direction): ItemExtractable {
        return ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))
    }

    @Environment(EnvType.CLIENT)
    override fun shouldRender(distance: Double): Boolean {
        var d = 16.0
        d *= 64.0 * getRenderDistanceMultiplier()
        return distance < d * d
    }

    override fun getEyeHeight(pose: EntityPose?, dimensions: EntityDimensions?): Float {
        return 0.0f
    }

    override fun writeCustomDataToNbt(tag: NbtCompound) {
        super.writeCustomDataToNbt(tag)

        tag.putByte("Facing", facing.id.toByte())
        tag.putByte("Mk", mk.toByte())

        tag.put("Inventory", modules.toNbtList())
    }

    override fun readCustomDataFromNbt(tag: NbtCompound) {
        super.readCustomDataFromNbt(tag)

        if (tag.contains("Facing")) setFacing(Direction.byId(tag.getByte("Facing").toInt()))
        mk = if (tag.contains("Mk")) tag.getByte("Mk").toInt().coerceIn(MIN_MK, MAX_MK) else MIN_MK

        modules.clear()
        setModuleCount(moduleCountByMk(mk), false)
        if (tag.contains("Inventory")) modules.readNbtList(tag.getList("Inventory", 10))
    }

    override fun createSpawnPacket(): Packet<*> {
        return EntitySpawnS2CPacket(this, type, ((mk and 0x3) shl 3) or (facing.id and 0x7), decorationBlockPos)
    }

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)

        val entityData = packet.entityData
        val direction = Direction.byId(entityData and 0x7)
        val mk = entityData shr 3 and 0x3

        setFacing(direction)
        this.mk = mk.coerceIn(MIN_MK, MAX_MK)
        modules = ModuleInventory(
            moduleCountByMk(this.mk),
            ModuleContext.Entity(this),
            ModulePath.ROOT,
            TPModules.MOVERS
        )
    }

    override fun getWidthPixels(): Int {
        return 12
    }

    override fun getHeightPixels(): Int {
        return 12
    }

    override fun onBreak(entity: Entity?) {
        if (world.gameRules.getBoolean(GameRules.DO_ENTITY_DROPS)) {
            playSound(SoundEvents.BLOCK_PISTON_CONTRACT, 1f, 1f)

            val drops = mutableListOf<ItemStack>()
            drops.add(ItemStack(TranspositionerItem.getItem(mk)))
            drops.addAll(modules.clearToList())

            dropStacks(entity, drops)
        }
    }

    private fun dropStacks(entity: Entity?, stacks: Iterable<ItemStack>) {
        when {
            entity is PlayerEntity -> {
                if (!entity.isCreative) {
                    for (stack in stacks) {
                        if (!entity.inventory.insertStack(stack)) {
                            dropStackOnEntity(entity, stack)
                        }
                    }
                }
            }
            entity != null -> {
                for (stack in stacks) {
                    dropStackOnEntity(entity, stack)
                }
            }
            else -> {
                for (stack in stacks) {
                    dropStack(stack)
                }
            }
        }
    }

    private fun dropStackOnEntity(entity: Entity, stack: ItemStack) {
        val itemEntity = ItemEntity(world, entity.x, entity.y, entity.z, stack)
        itemEntity.setToDefaultPickupDelay()
        world.spawnEntity(itemEntity)
    }

    override fun onPlace() {
        playSound(SoundEvents.BLOCK_PISTON_EXTEND, 1f, 1f)
    }

    override fun tick() {
        super.tick()

        if (!world.isClient) {
            modules.forEach { it.move() }
        }
    }

    override fun interact(player: PlayerEntity, hand: Hand): ActionResult {
        return if (!player.shouldCancelInteraction()) {
            player.openHandledScreen(this)
            ActionResult.SUCCESS
        } else {
            ActionResult.PASS
        }
    }

    override fun getDefaultName(): Text {
        return tt(type.translationKey + ".mk" + mk)
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return TranspositionerScreenHandler(syncId, inv, this)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeInt(id)
    }

    override fun getModule(index: Int): Module? {
        return modules.getModule(index)
    }
}