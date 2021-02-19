package com.kneelawk.transpositioners.client.screen

import com.kneelawk.transpositioners.screen.ModuleConfiguratorScreenHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class ModuleConfiguratorScreen(gui: ModuleConfiguratorScreenHandler, inv: PlayerInventory, title: Text) :
    AbstractTPScreen<ModuleConfiguratorScreenHandler>(gui, inv.player, title)
