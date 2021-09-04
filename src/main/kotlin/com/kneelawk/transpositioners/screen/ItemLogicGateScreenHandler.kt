package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants.str
import com.kneelawk.transpositioners.client.screen.TPBackgroundPainters
import com.kneelawk.transpositioners.client.screen.TPScreenUtils.tooltipLine
import com.kneelawk.transpositioners.module.ItemLogicGateModule
import com.kneelawk.transpositioners.net.OpenModulePacketHandler
import com.kneelawk.transpositioners.net.OpenParentPacketHandler
import com.kneelawk.transpositioners.net.sendToServer
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.util.IconUtils
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import org.apache.logging.log4j.LogManager
import com.kneelawk.transpositioners.util.IconUtils.notState as notStateI
import com.kneelawk.transpositioners.util.TooltipUtils.notState as notStateT

class ItemLogicGateScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, override val module: ItemLogicGateModule
) : SyncedGuiDescription(TPScreenHandlers.ITEM_LOGIC_GATE_TYPE, syncId, playerInventory, module.gates, null), ModuleScreenHandler {
    companion object {
        private val LOGGER = LogManager.getLogger()

        private val NET_PARENT = McNetworkStack.SCREEN_HANDLER.subType(
            ItemLogicGateScreenHandler::class.java, str("item_logic_gate_screen_handler")
        )

        private val OPEN_PARENT = OpenParentPacketHandler(NET_PARENT.idSignal("OPEN_PARENT")) { playerInventory.player }
        private val OPEN_MODULE = OpenModulePacketHandler(NET_PARENT.idData("OPEN_MODULE")) { playerInventory.player }

        private val CHANGE_NOT_STATE_ID =
            NET_PARENT.idData("CHANGE_NOT_STATE").setServerReceiver { module.updateNotState(it.readBoolean()) }
    }

    private val changeNotState: WScalableButton

    private var slots1: WItemSlot? = null
    private var slots2: WItemSlot? = null

    init {
        setTitleAlignment(HorizontalAlignment.CENTER)

        val root = WPlainPanel()
        setRootPanel(root)
        root.insets = Insets.ROOT_PANEL

        val backButton = WScalableButton(LiteralText("<-"))
        root.add(backButton, 0, 0)
        backButton.onClick = { OPEN_PARENT.send(this) }

        changeNotState = WScalableButton(icon = notStateI(module.notState))
        changeNotState.tooltip = listOf(tooltipLine(notStateT(module.notState)))
        changeNotState.onClick = {
            CHANGE_NOT_STATE_ID.sendToServer(this) { it.writeBoolean(!module.notState) }
        }

        val gateCount = ItemLogicGateModule.gateCountByMk(module.mk)

        when (module.mk) {
            1    -> {
                root.add(createPlayerInventoryPanel(), 0, 3 * 18 + 9)

                slots1 = TPScreenHandlerUtils.addSlots(
                    root, module.gates, { OPEN_MODULE.send(this, it) }, 0, gateCount, 3 * 18 + 9, 18
                )
            }
            2    -> {
                root.add(createPlayerInventoryPanel(), 0, 4 * 18)

                slots1 = TPScreenHandlerUtils.addSlots(
                    root, module.gates, { OPEN_MODULE.send(this, it) }, 0, gateCount, 2 * 18 + 9, 18
                )

                root.add(changeNotState, 4 * 18, 3 * 18)
            }
            3    -> {
                root.add(createPlayerInventoryPanel(), 0, 6 * 18 + 9)

                slots1 = TPScreenHandlerUtils.addSlots(
                    root, module.gates, { OPEN_MODULE.send(this, it) }, 0, gateCount / 2, 9, 18 + 9
                )
                slots2 = TPScreenHandlerUtils.addSlots(
                    root, module.gates, { OPEN_MODULE.send(this, it) }, gateCount / 2, gateCount / 2, 9, 3 * 18 + 9
                )

                root.add(changeNotState, 4 * 18, 5 * 18 + 9)
            }
            else -> LOGGER.warn(
                "Opened GUI for transpositioner with invalid mk: ${module.mk}"
            )
        }

        root.validate(this)
    }

    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        super.addPainters()
        val painter = TPBackgroundPainters.moduleSlot(IconUtils.GATE_SLOT)
        slots1?.backgroundPainter = painter
        slots2?.backgroundPainter = painter
    }

    fun s2cChangeNotState(notState: Boolean) {
        changeNotState.icon = notStateI(notState)
        changeNotState.tooltip = listOf(tooltipLine(notStateT(notState)))
    }
}