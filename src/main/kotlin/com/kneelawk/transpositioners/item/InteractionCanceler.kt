package com.kneelawk.transpositioners.item

import net.minecraft.item.ItemUsageContext

interface InteractionCanceler {
    fun shouldCancelInteraction(usageContext: ItemUsageContext): Boolean
}