package com.kneelawk.transpositioners.client.render

import com.kneelawk.transpositioners.item.TranspositionerConfiguratorItem
import com.kneelawk.transpositioners.item.TranspositionerItem
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.*

object TPOutlineRenderer {
    private val PLACEMENT_OUTLINE = arrayOf(
        Line(Vec3f(0f, 1f, 0f), Vec3f(1f, 1f, 0f)),
        Line(Vec3f(0f, 1f, 0f), Vec3f(0f, 1f, 1f)),
        Line(Vec3f(1f, 1f, 0f), Vec3f(1f, 1f, 1f)),
        Line(Vec3f(0f, 1f, 1f), Vec3f(1f, 1f, 1f)),
        Line(Vec3f(0f, 1f, 0f), Vec3f(0.25f, 1f, 0.25f)),
        Line(Vec3f(1f, 1f, 0f), Vec3f(0.75f, 1f, 0.25f)),
        Line(Vec3f(0f, 1f, 1f), Vec3f(0.25f, 1f, 0.75f)),
        Line(Vec3f(1f, 1f, 1f), Vec3f(0.75f, 1f, 0.75f)),
        Line(Vec3f(0.25f, 1f, 0.25f), Vec3f(0.75f, 1f, 0.25f)),
        Line(Vec3f(0.25f, 1f, 0.25f), Vec3f(0.25f, 1f, 0.75f)),
        Line(Vec3f(0.75f, 1f, 0.25f), Vec3f(0.75f, 1f, 0.75f)),
        Line(Vec3f(0.25f, 1f, 0.75f), Vec3f(0.75f, 1f, 0.75f))
    )

    private data class Line(val start: Vec3f, val end: Vec3f)

    fun register() {
        WorldRenderEvents.BLOCK_OUTLINE.register(::handleOutline)
    }

    private fun handleOutline(
        renderCtx: WorldRenderContext, outlineCtx: WorldRenderContext.BlockOutlineContext
    ): Boolean {
        val client = MinecraftClient.getInstance()

        client.player?.let { player ->
            (client.crosshairTarget as? BlockHitResult)?.let { target ->
                for (hand in Hand.values()) {
                    val item = player.getStackInHand(hand).item

                    if (item is TranspositionerItem) {
                        renderPlacementOutline(renderCtx, outlineCtx, target)

                        return true
                    }

                    if (item is TranspositionerConfiguratorItem) {
                        return false
                    }
                }
            }
        }

        return true
    }

    private fun renderPlacementOutline(
        renderCtx: WorldRenderContext, outlineCtx: WorldRenderContext.BlockOutlineContext, target: BlockHitResult
    ) {
        val lines = renderCtx.consumers()!!.getBuffer(RenderLayer.LINES)

        val stack = renderCtx.matrixStack()
        stack.push()

        translateForFace(stack, target, outlineCtx)

        stack.translate(0.5, 0.5, 0.5)
        stack.multiply(target.side.rotationQuaternion)
        stack.translate(-0.5, -0.5, -0.5)

        val model = stack.peek().positionMatrix
        val normal = stack.peek().normalMatrix

        for (line in PLACEMENT_OUTLINE) {
            val start = Vector4f(line.start)
            val end = Vector4f(line.end)

            var dx = end.x - start.x
            var dy = end.y - start.y
            var dz = end.z - start.z
            val invLen = MathHelper.fastInverseSqrt(dx * dx + dy * dy + dz * dz)
            dx *= invLen
            dy *= invLen
            dz *= invLen

            lines.vertex(model, start.x, start.y, start.z).color(0f, 0f, 0f, 0.4f).normal(normal, dx, dy, dz).next()
            lines.vertex(model, end.x, end.y, end.z).color(0f, 0f, 0f, 0.4f).normal(normal, dx, dy, dz).next()
        }

        stack.pop()
    }

    private fun translateForFace(
        stack: MatrixStack, target: BlockHitResult, outlineCtx: WorldRenderContext.BlockOutlineContext
    ) {
        val blockPos = outlineCtx.blockPos()
        val hitPos = target.pos
        when (target.side!!) {
            Direction.DOWN -> translate(
                stack, Vec3d(blockPos.x.toDouble(), hitPos.y, blockPos.z.toDouble()), outlineCtx
            )
            Direction.UP -> translate(
                stack, Vec3d(blockPos.x.toDouble(), hitPos.y - 1.0, blockPos.z.toDouble()), outlineCtx
            )
            Direction.NORTH -> translate(
                stack, Vec3d(blockPos.x.toDouble(), blockPos.y.toDouble(), hitPos.z), outlineCtx
            )
            Direction.SOUTH -> translate(
                stack, Vec3d(blockPos.x.toDouble(), blockPos.y.toDouble(), hitPos.z - 1.0), outlineCtx
            )
            Direction.WEST -> translate(
                stack, Vec3d(hitPos.x, blockPos.y.toDouble(), blockPos.z.toDouble()), outlineCtx
            )
            Direction.EAST -> translate(
                stack, Vec3d(hitPos.x - 1.0, blockPos.y.toDouble(), blockPos.z.toDouble()), outlineCtx
            )
        }
    }

    private fun translate(stack: MatrixStack, pos: Vec3d, outlineCtx: WorldRenderContext.BlockOutlineContext) {
        stack.translate(
            pos.x - outlineCtx.cameraX(), pos.y - outlineCtx.cameraY(), pos.z - outlineCtx.cameraZ()
        )
    }
}