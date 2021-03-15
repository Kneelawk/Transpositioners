package com.kneelawk.transpositioners.module

import alexiil.mc.lib.attributes.item.filter.ItemFilter

interface ItemGateModule : Module {
    val itemFilter: ItemFilter

    fun shouldMove(): Boolean
}