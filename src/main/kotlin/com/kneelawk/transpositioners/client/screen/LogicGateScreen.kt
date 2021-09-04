package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.LogicGateScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class LogicGateScreen(gui: LogicGateScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<LogicGateScreenHandler>(gui, inv.player, title)
