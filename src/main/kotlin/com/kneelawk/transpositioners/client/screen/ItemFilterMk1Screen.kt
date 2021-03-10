package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemFilterMk1ScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemFilterMk1Screen(gui: ItemFilterMk1ScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ItemFilterMk1ScreenHandler>(gui, inv.player, title)
