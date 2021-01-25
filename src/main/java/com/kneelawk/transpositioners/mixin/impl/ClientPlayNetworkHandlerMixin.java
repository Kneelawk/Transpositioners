package com.kneelawk.transpositioners.mixin.impl;

import com.kneelawk.transpositioners.entity.TranspositionerEntity;
import com.kneelawk.transpositioners.entity.TranspositionerEntityTypes;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Inject(method = "onEntitySpawn", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void handleEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci, double x, double y, double z,
                                   Entity entity41, EntityType<?> entityType) {
        Entity newEntity = null;
        if (entityType == TranspositionerEntityTypes.INSTANCE.getTRANSPOSITIONER()) {
            newEntity = new TranspositionerEntity(world, new BlockPos(x, y, z), Direction.byId(packet.getEntityData()));
        }

        if (newEntity != null) {
            int i = packet.getId();
            newEntity.updateTrackedPosition(x, y, z);
            newEntity.refreshPositionAfterTeleport(x, y, z);
            newEntity.pitch = (float) (packet.getPitch() * 360) / 256.0F;
            newEntity.yaw = (float) (packet.getYaw() * 360) / 256.0F;
            newEntity.setEntityId(i);
            newEntity.setUuid(packet.getUuid());
            world.addEntity(i, newEntity);
        }
    }
}
