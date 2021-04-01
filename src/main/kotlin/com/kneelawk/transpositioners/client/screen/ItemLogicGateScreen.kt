package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ItemLogicGateScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ItemLogicGateScreen(gui: ItemLogicGateScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ItemLogicGateScreenHandler>(gui, inv.player, title)
