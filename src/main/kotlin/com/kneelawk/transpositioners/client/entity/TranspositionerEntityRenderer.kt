package com.kneelawk.transpositioners.client.entity

import com.kneelawk.transpositioners.TranspositionersConstants
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.BlockRenderManager
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.LightType
import kotlin.math.max

@Environment(EnvType.CLIENT)
class TranspositionerEntityRenderer(dispatcher: EntityRenderDispatcher) :
    EntityRenderer<TranspositionerEntity>(dispatcher) {
    companion object {
        val MODEL_ID = ModelIdentifier(TranspositionersConstants.identifier("transpositioner"), "")
    }

    private val client = MinecraftClient.getInstance()

    override fun getTexture(entity: TranspositionerEntity?): Identifier {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE
    }

    override fun render(
        entity: TranspositionerEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)

        val newLight = if (light == 0) 15728640 else light

        matrices.push()
        val direction: Direction = entity.horizontalFacing
        val vec3d = getPositionOffset(entity, tickDelta)
        matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ())
        matrices.translate(
            direction.offsetX.toDouble() * 7.5 / 16.0, direction.offsetY.toDouble() * 7.5 / 16.0, direction.offsetZ
                .toDouble() * 7.5 / 16.0
        )
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(entity.pitch))
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0f - entity.yaw))

        val blockRenderManager: BlockRenderManager = client.blockRenderManager
        val bakedModelManager = blockRenderManager.models.modelManager
        matrices.translate(-0.5, -0.5, -0.5)
        blockRenderManager.modelRenderer.render(
            matrices.peek(),
            vertexConsumers.getBuffer(TexturedRenderLayers.getEntitySolid()),
            null,
            bakedModelManager.getModel(MODEL_ID),
            1.0f,
            1.0f,
            1.0f,
            newLight,
            OverlayTexture.DEFAULT_UV
        )
        matrices.pop()
    }

    override fun getPositionOffset(entity: TranspositionerEntity, tickDelta: Float): Vec3d {
        return Vec3d(
            entity.horizontalFacing.offsetX * 0.3,
            entity.horizontalFacing.offsetY * 0.3,
            entity.horizontalFacing.offsetZ * 0.3
        )
    }

    override fun method_27950(entity: TranspositionerEntity, blockPos: BlockPos): Int {
        val pos1 = entity.decorationBlockPos
        val pos2 = pos1.offset(entity.horizontalFacing)
        return max(entity.world.getLightLevel(LightType.SKY, pos1), entity.world.getLightLevel(LightType.SKY, pos2))
    }

    override fun getBlockLight(entity: TranspositionerEntity, blockPos: BlockPos): Int {
        val pos1 = entity.decorationBlockPos
        val pos2 = pos1.offset(entity.horizontalFacing)
        return max(entity.world.getLightLevel(LightType.BLOCK, pos1), entity.world.getLightLevel(LightType.BLOCK, pos2))
    }
}