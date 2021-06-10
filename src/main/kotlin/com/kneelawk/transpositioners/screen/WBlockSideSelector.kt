package com.kneelawk.transpositioners.screen

import com.kneelawk.transpositioners.client.render.TranspositionerGhostRenderer
import com.kneelawk.transpositioners.client.screen.icon.FramebufferIcon
import com.kneelawk.transpositioners.client.util.TPModels
import com.kneelawk.transpositioners.util.IconUtils
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.BlockRenderManager
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.*
import net.minecraft.world.BlockRenderView
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt


class WBlockSideSelector(
    private val world: BlockRenderView?, var pos: BlockPos, width: Int, height: Int,
    initialSelectedSides: Set<Direction> = setOf()
) :
    WWidget(),
    AutoCloseable {
    companion object {

        private val sides = arrayOf(
            Side(Vec3f(0.5f, 0f, 0.5f), Direction.DOWN),
            Side(Vec3f(0.5f, 1f, 0.5f), Direction.UP),
            Side(Vec3f(0.5f, 0.5f, 0f), Direction.NORTH),
            Side(Vec3f(0.5f, 0.5f, 1f), Direction.SOUTH),
            Side(Vec3f(0f, 0.5f, 0.5f), Direction.WEST),
            Side(Vec3f(1f, 0.5f, 0.5f), Direction.EAST)
        )

        /**
         * Determines the point of intersection between a plane defined by a point and a normal vector and a line defined by a point and a direction vector.
         *
         * Copied from StackOverflow: <a href="https://stackoverflow.com/a/52711312/1687581">https://stackoverflow.com/a/52711312/1687581</a>
         *
         * @param planePoint    A point on the plane.
         * @param planeNormal   The normal vector of the plane.
         * @param linePoint     A point on the line.
         * @param lineDirection The direction vector of the line.
         * @return The point of intersection between the line and the plane, null if the line is parallel to the plane.
         */
        private fun lineIntersection(
            planePoint: Vec3f, planeNormal: Vec3f, linePoint: Vec3f, lineDirection: Vec3f
        ): Vec3f? {
            if (abs(planeNormal.dot(lineDirection)) < 1.0E-5) {
                return null
            }

            val t = (planeNormal.dot(planePoint) - planeNormal.dot(linePoint)) / planeNormal.dot(lineDirection)
            lineDirection.scale(t)
            linePoint.add(lineDirection)
            return linePoint
        }
    }

    init {
        this.width = width
        this.height = height
    }

    private var icon: FramebufferIcon? = null

    private var distanceMoved = 0.0
    private var rotationSetup = false
    var pitch = 45f
    var yaw = 45f

    val selectedSides = mutableSetOf<Direction>()
    var onSideClicked: ((Direction, Int) -> Unit)? = null

    var blockScale = 0.5f

    init {
        selectedSides.addAll(initialSelectedSides)
    }

    var selectedSide: Direction?
        get() = selectedSides.firstOrNull()
        set(value) {
            selectedSides.clear()
            value?.let { selectedSides.add(it) }
        }

    fun resetRotation() {
        rotationSetup = false
    }

    override fun setSize(x: Int, y: Int) {
        // size is constant
    }

    @Environment(EnvType.CLIENT)
    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        setupFramebuffer()
        setupRotation()

        val icon = icon ?: return
        val world = world ?: return
        val fb = icon.framebuffer
        val mc = MinecraftClient.getInstance()
        val blockRenderManager: BlockRenderManager = mc.blockRenderManager
        val bakedModelManager = blockRenderManager.models.modelManager

        fb.clear(MinecraftClient.IS_SYSTEM_MAC)

        fb.setTexFilter(
            if ((mc.window.scaleFactor.toInt() % 2) == 0) GL11.GL_NEAREST else GL11.GL_LINEAR
        )
        fb.beginWrite(true)

        val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
        val blockStack = buildMatrix()
        val random = Random(42)

        val blockState = world.getBlockState(pos)
        if (!blockState.isAir) {
            val bufferBuilder = immediate.getBuffer(RenderLayers.getBlockLayer(blockState))
            blockRenderManager.renderBlock(blockState, pos, world, blockStack, bufferBuilder, false, random)
        }

        val fluidState = world.getFluidState(pos)
        if (!fluidState.isEmpty) {
            val bufferBuilder = immediate.getBuffer(RenderLayers.getFluidLayer(fluidState))
            blockRenderManager.renderFluid(pos, world, bufferBuilder, fluidState)
        }

        world.getBlockEntity(pos)?.let { be ->
            mc.blockEntityRenderDispatcher.render(be, mc.tickDelta, blockStack, immediate)
        }

        // Draw the selected block to the gui and then draw the side selectors over top of it
        immediate.draw()
        mc.framebuffer.beginWrite(true)
        icon.paint(matrices, x, y, width, height)
        fb.clear(MinecraftClient.IS_SYSTEM_MAC)
        fb.beginWrite(true)

        for (side in selectedSides) {
            blockRenderManager.modelRenderer.render(
                blockStack.peek(),
                immediate.getBuffer(TranspositionerGhostRenderer.GHOST),
                null,
                bakedModelManager.getModel(TPModels.SIDE_SELECTS[side.id]),
                1.0f,
                1.0f,
                1.0f,
                15728640,
                OverlayTexture.DEFAULT_UV
            )
        }

        if (isWithinBounds(mouseX, mouseY)) {
            getHitSide(mouseX, mouseY)?.let { side ->
                blockRenderManager.modelRenderer.render(
                    blockStack.peek(),
                    immediate.getBuffer(TranspositionerGhostRenderer.PLACEMENT),
                    null,
                    bakedModelManager.getModel(TPModels.SIDE_SELECTS[side.id]),
                    1.0f,
                    1.0f,
                    1.0f,
                    15728640,
                    OverlayTexture.DEFAULT_UV
                )
            }
        }

        // Draw the side selectors to the gui
        immediate.draw()
        mc.framebuffer.beginWrite(true)
        icon.paint(matrices, x, y, width, height)

        IconUtils.BORDER_INSET.paint(matrices, x, y, width, height)
    }

    private fun setupFramebuffer() {
        if (icon == null) {
            val icon = FramebufferIcon(width, height, true)
            icon.framebuffer.setClearColor(0.4f, 0.4f, 0.4f, 0.0f)
            this.icon = icon
        }
    }

    private fun setupRotation() {
        if (!rotationSetup) {
            rotationSetup = true

            val mc = MinecraftClient.getInstance()
            mc.player?.let {
                pitch = it.pitch
                yaw = it.yaw + 180f
            }
        }
    }

    private fun buildMatrix(): MatrixStack {
        val mc = MinecraftClient.getInstance()
        val blockStack = MatrixStack()
        blockStack.scale(
            (mc.window.width / mc.window.scaleFactor).toFloat(), (mc.window.height / mc.window.scaleFactor).toFloat(),
            1f
        )
        blockStack.translate(0.0, 1.0, 0.0)
        blockStack.scale(1f, -1f, 1f)
//        blockStack.peek().model.multiply(Matrix4f.projectionMatrix(2f, 2f, 0.1f, 1000f))
//        blockStack.peek().model.multiply(Matrix4f.viewboxMatrix(45.0, 1f, 0.05f, 1000f))
//        blockStack.translate(0.0, 0.0, -5.0)
        blockStack.translate(0.5, 0.5, 0.5)
        blockStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(pitch))
        blockStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw))
        blockStack.scale(blockScale, blockScale, blockScale)
        blockStack.translate(-0.5, -0.5, -0.5)
        return blockStack
    }

    @Environment(EnvType.CLIENT)
    override fun onMouseDrag(x: Int, y: Int, button: Int, deltaX: Double, deltaY: Double): InputResult {
        yaw += deltaX.toFloat() * 2f
        pitch += deltaY.toFloat() * 2f
        distanceMoved += sqrt(deltaX * deltaX + deltaY * deltaY)
        return InputResult.PROCESSED
    }

    @Environment(EnvType.CLIENT)
    override fun onClick(x: Int, y: Int, button: Int): InputResult {
        val mc = MinecraftClient.getInstance()
        if (distanceMoved < 4.0 / mc.window.scaleFactor && isWithinBounds(x, y)) {
            getHitSide(x, y)?.let { hit ->
                mc.soundManager.play(
                    PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)
                )
                onSideClicked?.invoke(hit, button)
            }
        }
        distanceMoved = 0.0
        return InputResult.PROCESSED
    }

    private fun getHitSide(x: Int, y: Int): Direction? {
        val mat = Matrix4f()
        mat.loadIdentity()
        mat.multiply(Matrix4f.translate(0.5f, 0.5f, 0.5f))
        mat.multiply(Matrix4f.scale(1f / blockScale, 1f / blockScale, 1f / blockScale))
        mat.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw))
        mat.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-pitch))
        mat.multiply(Matrix4f.translate(-0.5f, -0.5f, -0.5f))
        mat.multiply(Matrix4f.translate(0.0f, 1.0f, 0.0f))
        mat.multiply(Matrix4f.scale(1f / width.toFloat(), -1f / height.toFloat(), 1f))

        val resultingLoc = Vector4f(x.toFloat() + 0.5f, y.toFloat() + 0.5f, 5f, 1f)
        resultingLoc.transform(mat)

        val rot = Matrix4f()
        rot.loadIdentity()
        rot.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw))
        rot.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-pitch))

        val resultingNorm = Vector4f(0f, 0f, -1f, 0f)
        resultingNorm.transform(rot)
        resultingNorm.normalize()

        val clickLoc = Vec3f(resultingLoc.x, resultingLoc.y, resultingLoc.z)
        val clickNorm = Vec3f(resultingNorm.x, resultingNorm.y, resultingNorm.z)

        var selectedSide: Direction? = null
        var selectedDistance = 10000f
        for (side in sides) {
            side.intersects(clickLoc, clickNorm)?.let { hit ->
                val difX = clickLoc.x - hit.x
                val difY = clickLoc.y - hit.y
                val difZ = clickLoc.z - hit.z
                val hitDistance = difX * difX + difY * difY + difZ * difZ
                if (hitDistance < selectedDistance) {
                    selectedDistance = hitDistance
                    selectedSide = side.direction
                }
            }
        }

        return selectedSide
    }

    override fun close() {
        icon?.framebuffer?.delete()
    }

    private class Side(val point: Vec3f, val direction: Direction) {
        fun intersects(clickLoc: Vec3f, clickNorm: Vec3f): Vec3f? {
            val i = lineIntersection(point, direction.unitVector, clickLoc.copy(), clickNorm.copy()) ?: return null

            return if (i.x > -0.00001 && i.x < 1.00001 && i.y > -0.00001 && i.y < 1.00001 && i.z > -0.00001 && i.z < 1.00001) i else null
        }
    }
}