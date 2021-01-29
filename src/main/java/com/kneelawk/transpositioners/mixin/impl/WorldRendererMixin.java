package com.kneelawk.transpositioners.mixin.impl;

import com.kneelawk.transpositioners.client.render.TranspositionerGhostRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
            at = @At(value = "CONSTANT", args = "stringValue=particles"))
    private void handleRender(MatrixStack matrices, float tickDelta, long startTime, boolean renderBlockOutline,
                              Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                              Matrix4f matrix4f, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            TranspositionerGhostRenderer.INSTANCE.draw(camera, tickDelta, matrices);
        }
    }
}
