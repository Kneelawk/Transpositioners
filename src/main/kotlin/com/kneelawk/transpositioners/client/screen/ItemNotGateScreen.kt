package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemNotGateScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemNotGateScreen(gui: ItemNotGateScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ItemNotGateScreenHandler>(gui, inv.player, title)
