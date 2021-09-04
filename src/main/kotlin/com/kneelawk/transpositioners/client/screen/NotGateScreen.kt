package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.NotGateScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class NotGateScreen(gui: NotGateScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<NotGateScreenHandler>(gui, inv.player, title)
