package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemMoverMk2ScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemMoverMk2Screen(gui: ItemMoverMk2ScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ItemMoverMk2ScreenHandler>(gui, inv.player, title)
