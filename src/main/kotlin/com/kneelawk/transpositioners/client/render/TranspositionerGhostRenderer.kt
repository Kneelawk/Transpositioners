package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.client.entity.TranspositionerEntityRenderer
import com.kneelawk.transpositioners.item.TranspositionerItem
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Matrix4f

object TranspositionerGhostRenderer {
    private val RENDER_LAYERS = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>()
    private val CONSUMERS: VertexConsumerProvider.Immediate =
        VertexConsumerProvider.immediate(RENDER_LAYERS, BufferBuilder(1 shl 12))

    private val origStack = MatrixStack()
    private val origModel = Matrix4f()

    init {
        RENDER_LAYERS[TPRenderLayers.TRANSPOSITIONER_GHOST] = BufferBuilder(1 shl 12)
        RENDER_LAYERS[TPRenderLayers.TRANSPOSITIONER_PLACEMENT] = BufferBuilder(1 shl 12)
    }

    fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            // Copy matrices from AFTER_ENTITIES because this is when they seem to be intact in both Indigo and Canvas.
            val stack = context.matrixStack()
            origStack.peek().model.load(stack.peek().model)
            origStack.peek().normal.load(stack.peek().normal)
            origModel.load(RenderSystem.getModelViewMatrix())
        }
        WorldRenderEvents.END.register { context ->
            // Render in END because our translucent placement ghost would occlude chests otherwise.
            draw(context.camera(), context.tickDelta())
        }
    }

    fun getVertexConsumer(renderLayer: RenderLayer): VertexConsumer {
        return CONSUMERS.getBuffer(renderLayer)
    }

    private fun draw(
        camera: Camera,
        tickDelta: Float
    ) {
        val matrices = origStack

        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            val world = player.world

            // Render the placement ghost
            val target = client.crosshairTarget

            if (target is BlockHitResult && target.type == HitResult.Type.BLOCK) {
                for (hand in Hand.values()) {
                    val stack = player.getStackInHand(hand)
                    val item = stack.item
                    if (item is TranspositionerItem) {
                        val entity = item.getTranspositionerPlacement(
                            world,
                            player,
                            target.blockPos,
                            target.side,
                            stack,
                            target.pos
                        )
                        if (entity != null) {
                            val cameraPos = camera.pos
                            val entityOffset = TranspositionerEntityRenderer.getPositionOffset(entity, tickDelta)
                            val x = entity.x - cameraPos.x + entityOffset.x
                            val y = entity.y - cameraPos.y + entityOffset.y
                            val z = entity.z - cameraPos.z + entityOffset.z
                            matrices.push()
                            matrices.translate(x, y, z)
                            TranspositionerEntityRenderer.render(
                                entity,
                                tickDelta,
                                matrices,
                                CONSUMERS.getBuffer(TPRenderLayers.TRANSPOSITIONER_PLACEMENT),
                                0
                            )
                            matrices.pop()
                        }

                        break
                    }
                }
            }
        }

        val stack = RenderSystem.getModelViewStack()
        stack.push()
        stack.method_34425(origModel)
        RenderSystem.applyModelViewMatrix()

        CONSUMERS.draw()

        stack.pop()
        RenderSystem.applyModelViewMatrix()
    }
}