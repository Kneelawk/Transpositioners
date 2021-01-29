package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.TranspositionerScreenHandler
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class TranspositionerScreen(gui: TranspositionerScreenHandler, inv: PlayerInventory, title: Text) :
    CottonInventoryScreen<TranspositionerScreenHandler>(gui, inv.player, title)
