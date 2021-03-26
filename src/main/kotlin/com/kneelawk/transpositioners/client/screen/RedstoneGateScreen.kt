package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.RedstoneGateScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class RedstoneGateScreen(gui: RedstoneGateScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<RedstoneGateScreenHandler>(gui, inv.player, title)
