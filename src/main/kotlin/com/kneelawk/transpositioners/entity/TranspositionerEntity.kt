package com.kneelawk.transpositioners.entity

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
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
import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.item.TranspositionerItem
import com.kneelawk.transpositioners.module.ModuleContainer
import com.kneelawk.transpositioners.module.MoverModule
import com.kneelawk.transpositioners.module.TranspositionerModule
import com.kneelawk.transpositioners.module.TranspositionerModules
import com.kneelawk.transpositioners.screen.TranspositionerScreenHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.*
import net.minecraft.entity.decoration.AbstractDecorationEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.GameRules
import net.minecraft.world.World
import org.apache.commons.lang3.Validate

class TranspositionerEntity : AbstractDecorationEntity, ExtendedScreenHandlerFactory, ModuleContainer {
    companion object {
        val NET_PARENT: ParentNetIdSingle<TranspositionerEntity> = McNetworkStack.ENTITY.subType(
            TranspositionerEntity::class.java,
            TranspositionersConstants.str("transpositioner_entity")
        )
        val ID_CHANGE_MK: NetIdDataK<TranspositionerEntity> =
            NET_PARENT.idData("CHANGE_MK").setReceiver(TranspositionerEntity::receiveMkChange)

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
    val modules = arrayListOf<MoverModule?>()
    // TODO: add inventory

    constructor(entityType: EntityType<out TranspositionerEntity>, world: World) : super(entityType, world) {
        mk = MIN_MK
        setModuleCount(moduleCountByMk(mk), TranspositionerModule.RemovalType.DELETE)
    }

    constructor(world: World, pos: BlockPos, direction: Direction, mk: Int) : super(
        TranspositionerEntityTypes.TRANSPOSITIONER,
        world,
        pos
    ) {
        setFacing(direction)
        this.mk = mk.coerceIn(MIN_MK, MAX_MK)
        setModuleCount(moduleCountByMk(mk), TranspositionerModule.RemovalType.DELETE)
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

    private fun setModuleCount(count: Int, removalType: TranspositionerModule.RemovalType) {
        if (count > modules.size) {
            modules.ensureCapacity(count)
            for (i in modules.size until count) {
                modules.add(null)
            }
        } else if (count < modules.size) {
            for (i in (count until modules.size).reversed()) {
                val module = modules.removeLast()
                module?.onRemove(removalType)
            }
        }
    }

    fun updateMk(mk: Int) {
        this.mk = mk.coerceIn(MIN_MK, MAX_MK)

        if (!world.isClient) {
            sendMkChage(this.mk)

            // TODO: setModuleCount and stuff
        }

        // TODO: Inventory re-organization
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
        updateMk(buf.readByte().toInt())
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

    override fun writeCustomDataToTag(tag: CompoundTag) {
        super.writeCustomDataToTag(tag)

        tag.putByte("Facing", facing.id.toByte())
        tag.putByte("Mk", mk.toByte())

        val moduleList = ListTag()
        for (index in modules.indices) {
            modules.getOrNull(index)?.let { module ->
                val holder = CompoundTag()
                holder.putInt("index", index)
                holder.putString("type", module.type.id.toString())
                val moduleTag = CompoundTag()
                module.writeToTag(moduleTag)
                holder.put("module", moduleTag)
                moduleList.add(holder)
            }
        }

        tag.put("Modules", moduleList)
    }

    override fun readCustomDataFromTag(tag: CompoundTag) {
        super.readCustomDataFromTag(tag)

        if (tag.contains("Facing")) setFacing(Direction.byId(tag.getByte("Facing").toInt()))
        mk = if (tag.contains("Mk")) tag.getByte("Mk").toInt().coerceIn(MIN_MK, MAX_MK) else MIN_MK

        modules.clear()
        setModuleCount(moduleCountByMk(mk), TranspositionerModule.RemovalType.DELETE)
        for (holderTag in tag.getList("Modules", 10)) {
            val holder = holderTag as CompoundTag
            val index = holder.getInt("index")
            if (index >= 0 && index < modules.size) {
                val type = TranspositionerModules.getMoverById(Identifier(holder.getString("type")))
                val moduleTag = holder.getCompound("module")
                // TODO: Inventory loading
                val module = type.readFromTag(this, index, ItemStack.EMPTY, moduleTag)
                modules[index] = module
            }
        }
    }

    override fun createSpawnPacket(): Packet<*> {
        return EntitySpawnS2CPacket(this, type, ((mk and 0x3) shl 3) or (facing.id and 0x7), decorationBlockPos)
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
            // TODO: drop own inventory

            for (module in modules) {
                module?.addStacksForDrop(drops)
            }

            dropStacks(entity, drops)
        }

        for (module in modules) {
            module?.onRemove(TranspositionerModule.RemovalType.DROP)
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
            val extract = getItemExtractable(attachmentPos.offset(facing.opposite), facing.opposite)
            val insert = getItemInsertable(attachmentPos, facing)

            val maxAmount = 8

            val extractedSim = extract.attemptAnyExtraction(maxAmount, Simulation.SIMULATE)
            if (!extractedSim.isEmpty) {
                val leftOverSim = insert.attemptInsertion(extractedSim, Simulation.SIMULATE)
                val amount = extractedSim.count - leftOverSim.count

                if (amount != 0) {
                    val leftOver =
                        insert.attemptInsertion(
                            extract.attemptAnyExtraction(amount, Simulation.ACTION),
                            Simulation.ACTION
                        )
                    assert(leftOver.isEmpty) { "leftOver: $leftOver" }
                }
            }
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
        return TranslatableText(type.translationKey + ".mk" + mk)
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return TranspositionerScreenHandler(syncId, inv, this, ScreenHandlerContext.EMPTY)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeInt(entityId)
    }

    override fun getModule(index: Int): TranspositionerModule? {
        return modules.getOrNull(index)
    }
}