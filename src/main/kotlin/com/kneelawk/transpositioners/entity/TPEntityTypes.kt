package com.kneelawk.transpositioners.entity

import com.kneelawk.transpositioners.TPConstants
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.registry.Registry

object TPEntityTypes {
    val TRANSPOSITIONER_ID = TPConstants.identifier("transpositioner")

    val TRANSPOSITIONER: EntityType<TranspositionerEntity> =
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::TranspositionerEntity)
            .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(10).trackedUpdateRate(Integer.MAX_VALUE)
            .build()

    fun register() {
        Registry.register(Registry.ENTITY_TYPE, TRANSPOSITIONER_ID, TRANSPOSITIONER)
    }
}