package com.kneelawk.transpositioners.client.render

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f

object TPMatrixFixer {
    private val origStack = MatrixStack()
    private val origModel = Matrix4f()

    fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            // Copy matrices from AFTER_ENTITIES because this is when they seem to be intact in both Indigo and Canvas.
            val stack = context.matrixStack()
            origStack.peek().model.load(stack.peek().model)
            origStack.peek().normal.load(stack.peek().normal)
            origModel.load(RenderSystem.getModelViewMatrix())
        }
    }

    fun getStack(): MatrixStack = origStack

    fun renderSystemPush() {
        val stack = RenderSystem.getModelViewStack()
        stack.push()
        stack.method_34425(origModel)
        RenderSystem.applyModelViewMatrix()
    }

    fun renderSystemPop() {
        RenderSystem.getModelViewStack().pop()
        RenderSystem.applyModelViewMatrix()
    }
}