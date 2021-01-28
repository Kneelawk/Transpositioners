package com.kneelawk.transpositioners.item

import com.kneelawk.transpositioners.entity.TranspositionerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.function.Predicate

object TranspositionerItemUtils {

    fun tryOpenTranspositioner(world: World, player: PlayerEntity): Boolean {
        return raycast(player) != null
    }

    private fun raycast(player: PlayerEntity): TranspositionerEntity? {
        return raycastForPlayer(player)?.entity as? TranspositionerEntity
    }

    private fun raycastForPlayer(player: PlayerEntity): EntityHitResult? {
        val reachDistance = if (player.isCreative) 5.0 else 4.5

        val rayStart: Vec3d = player.getCameraPosVec(1.0f)
        val direction = player.getRotationVec(1.0f)
        val rayEnd = rayStart.add(direction.multiply(reachDistance))
        val reachSquared = reachDistance * reachDistance

        val box: Box = player
            .boundingBox
            .stretch(player.getRotationVec(1.0f).multiply(reachDistance))
            .expand(1.0, 1.0, 1.0)

        return raycast(
            player,
            rayStart,
            rayEnd,
            box,
            { entityx: Entity -> !entityx.isSpectator && entityx.collides() && entityx is TranspositionerEntity },
            reachSquared
        )
    }

    private fun raycast(
        entity: Entity,
        rayStart: Vec3d,
        rayEnd: Vec3d,
        box: Box,
        predicate: Predicate<Entity>?,
        reachSquared: Double
    ): EntityHitResult? {
        val world = entity.world
        var e = reachSquared
        var entity2: Entity? = null
        var vec3d3: Vec3d? = null
        val var12: Iterator<*> = world.getOtherEntities(entity, box, predicate).iterator()
        while (true) {
            while (var12.hasNext()) {
                val entity3 = var12.next() as Entity
                val box2 = entity3.boundingBox.expand(entity3.targetingMargin.toDouble())
                val optional = box2.raycast(rayStart, rayEnd)
                if (box2.contains(rayStart)) {
                    if (e >= 0.0) {
                        entity2 = entity3
                        vec3d3 = optional.orElse(rayStart) as Vec3d
                        e = 0.0
                    }
                } else if (optional.isPresent) {
                    val vec3d4 = optional.get()
                    val f = rayStart.squaredDistanceTo(vec3d4)
                    if (f < e || e == 0.0) {
                        if (entity3.rootVehicle === entity.rootVehicle) {
                            if (e == 0.0) {
                                entity2 = entity3
                                vec3d3 = vec3d4
                            }
                        } else {
                            entity2 = entity3
                            vec3d3 = vec3d4
                            e = f
                        }
                    }
                }
            }
            return entity2?.let { EntityHitResult(it, vec3d3) }
        }
    }
}