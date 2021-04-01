package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemGateMk1ScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemGateMk1Screen(gui: ItemGateMk1ScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ItemGateMk1ScreenHandler>(gui, inv.player, title)
