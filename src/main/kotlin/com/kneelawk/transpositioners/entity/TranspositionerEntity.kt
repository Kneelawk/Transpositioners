package com.kneelawk.transpositioners.entity

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemExtractable
import alexiil.mc.lib.attributes.item.ItemInsertable
import alexiil.mc.lib.attributes.item.impl.EmptyItemExtractable
import alexiil.mc.lib.attributes.item.impl.RejectingItemInsertable
import com.kneelawk.transpositioners.item.TranspositionerItems
import com.kneelawk.transpositioners.screen.TranspositionerScreenHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.*
import net.minecraft.entity.decoration.AbstractDecorationEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.GameRules
import net.minecraft.world.World
import org.apache.commons.lang3.Validate

class TranspositionerEntity : AbstractDecorationEntity, ExtendedScreenHandlerFactory {
    constructor(entityType: EntityType<out TranspositionerEntity>, world: World) : super(entityType, world)
    constructor(world: World, pos: BlockPos, direction: Direction) : super(
        TranspositionerEntityTypes.TRANSPOSITIONER,
        world,
        pos
    ) {
        setFacing(direction)
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
    }

    override fun readCustomDataFromTag(tag: CompoundTag) {
        super.readCustomDataFromTag(tag)

        setFacing(Direction.byId(tag.getByte("Facing").toInt()))
    }

    override fun createSpawnPacket(): Packet<*> {
        return EntitySpawnS2CPacket(this, type, facing.id, decorationBlockPos)
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

            // TODO: handle entity recursive inventory stuff.
            dropStacks(entity, listOf(ItemStack(TranspositionerItems.TRANSPOSITIONER)))
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

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return TranspositionerScreenHandler(syncId, inv, this, ScreenHandlerContext.EMPTY)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeInt(entityId)
    }
}