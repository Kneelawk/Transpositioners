package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemMoverMk3ScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemMoverMk3Screen(gui: ItemMoverMk3ScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ItemMoverMk3ScreenHandler>(gui, inv.player, title)
