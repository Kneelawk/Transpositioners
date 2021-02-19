package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.TranspositionerScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class TranspositionerScreen(gui: TranspositionerScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<TranspositionerScreenHandler>(gui, inv.player, title)
