package com.kneelawk.transpositioners.screen

import alexiil.mc.lib.net.NetIdDataK
import alexiil.mc.lib.net.ParentNetIdSingle
import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.transpositioners.TPConstants
import com.kneelawk.transpositioners.TPConstants.gui
import com.kneelawk.transpositioners.TPConstants.tooltip
import com.kneelawk.transpositioners.client.screen.TPScreenUtils.tooltipLine
import com.kneelawk.transpositioners.entity.TranspositionerEntity
import com.kneelawk.transpositioners.net.NetScreenHandlerScope
import com.kneelawk.transpositioners.net.setClientReceiver
import com.kneelawk.transpositioners.net.setServerReceiver
import com.kneelawk.transpositioners.proxy.CommonProxy
import com.kneelawk.transpositioners.screen.TPScreenHandlerUtils.addSlots
import com.kneelawk.transpositioners.util.IconUtils
import com.kneelawk.transpositioners.util.TooltipUtils.lockedExtra
import com.kneelawk.transpositioners.util.TooltipUtils.lockedState
import com.kneelawk.transpositioners.util.orNull
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import org.apache.logging.log4j.LogManager
import java.util.*

class TranspositionerScreenHandler(
    syncId: Int,
    override val playerInventory: PlayerInventory,
    val entity: TranspositionerEntity
) : SyncedGuiDescription(
    TPScreenHandlers.TRANSPOSITIONER_TYPE,
    syncId,
    playerInventory,
    entity.modules,
    null
), NetScreenHandlerScope<TranspositionerScreenHandler> {

    companion object {
        private val LOGGER = LogManager.getLogger()

        private val NET_PARENT: ParentNetIdSingle<TranspositionerScreenHandler> = McNetworkStack.SCREEN_HANDLER.subType(
            TranspositionerScreenHandler::class.java,
            TPConstants.str("transpositioner_screen_handler")
        )

        private val ID_OPEN_MODULE: NetIdDataK<TranspositionerScreenHandler> =
            NET_PARENT.idData("OPEN_MODULE").setServerReceiver { buf ->
                if (entity.hasPermission(player)) {
                    (entity.modules.getModule(buf.readVarInt()) as? NamedScreenHandlerFactory)?.let {
                        player.openHandledScreen(it)
                    }
                }
                buf.clear()
            }

        private val ID_PLAYER_LIST = NET_PARENT.idData("PLAYER_LIST").setClientReceiver { buf ->
            val owner = if (buf.readBoolean()) {
                buf.readString()
            } else null

            val playerCount = buf.readVarUnsignedInt()
            val newPlayerList = mutableListOf<String>()
            for (i in 0 until playerCount) {
                newPlayerList.add(buf.readString())
            }

            ownerText?.let { text ->
                if (owner == null) {
                    text.text = gui("no_owner")
                } else {
                    text.text = gui("owner", LiteralText(owner).formatted(Formatting.DARK_BLUE))
                }
            }

            playerList.clear()
            playerList.addAll(newPlayerList)
            playerSet.clear()
            playerSet.addAll(newPlayerList.asSequence().map { it.lowercase() })
            playerListWidget?.layout()
            playerTextField?.let { field ->
                val playerName = field.text
                val contains = playerSet.contains(playerName.lowercase())
                addPlayerButton?.enabled = !contains && playerName.isNotBlank()
                removePlayerButton?.enabled = contains
            }
        }

        private val ID_LOCKED_CHANGE = NET_PARENT.idData("LOCKED_CHANGE").setServerReceiver {
            if (entity.hasEditPermission(player)) {
                entity.updateLocked(it.readBoolean())
            }
            it.clear()
        }

        private val ID_ADD_PLAYER = NET_PARENT.idData("ADD_PLAYER").setServerReceiver {
            if (entity.hasEditPermission(player)) {
                forPlayer(it.readString(), entity::addPermissionPlayer)
            }
            it.clear()
        }

        private val ID_REMOVE_PLAYER = NET_PARENT.idData("REMOVE_PLAYER").setServerReceiver {
            if (entity.hasEditPermission(player)) {
                forPlayer(it.readString(), entity::removePermissionPlayer)
            }
            it.clear()
        }
    }

    private val playerList = mutableListOf<String>()
    private val playerSet = mutableSetOf<String>()
    private var ownerText: WText? = null
    private var playerListWidget: WListPanel<String, WScalableButton>? = null
    private var lockedButton: WScalableButton? = null
    private var playerTextField: WSpecialTextField? = null
    private var addPlayerButton: WScalableButton? = null
    private var removePlayerButton: WScalableButton? = null

    init {
        setTitleAlignment(HorizontalAlignment.CENTER)

        val root = WCardPanel()
        setRootPanel(root)
        val editPermission = entity.hasEditPermission(player)

        // inventory panel

        val inventoryPanel = WPlainPanel()
        inventoryPanel.insets = Insets.ROOT_PANEL

        val permissionsButton = if (editPermission) {
            val permissionsButton = WScalableButton(icon = IconUtils.LOCKED)
            permissionsButton.tooltip = listOf(tooltipLine(tooltip("edit_permissions")))
            permissionsButton.onClick = {
                root.selectedIndex = 1
            }
            permissionsButton
        } else {
            null
        }

        val moduleCount = TranspositionerEntity.moduleCountByMk(entity.mk)

        when (entity.mk) {
            1 -> {
                inventoryPanel.add(createPlayerInventoryPanel(), 0, 3 * 18 + 9)

                val slots = WPlainPanel()
                val slot = WItemSlot.of(entity.modules, 0)
                slots.add(slot, 3 * 18 + 9, 0)
                val button = WScalableButton(LiteralText("..."))
                button.enabled = entity.modules.getModule(0) is NamedScreenHandlerFactory
                slots.add(button, 4 * 18 + 9, 0)
                button.onClick = {
                    sendOpenModule(0)
                }

                slot.addChangeListener { _, _, _, _ ->
                    button.enabled = entity.modules.getModule(0) is NamedScreenHandlerFactory
                }

                inventoryPanel.add(slots, 0, 18, 9 * 18, 18)

                permissionsButton?.let { inventoryPanel.add(it, 8 * 18, 3 * 18, 18, 18) }
            }
            2 -> {
                inventoryPanel.add(createPlayerInventoryPanel(), 0, 3 * 18 + 9)

                val slots = WPlainPanel()
                addSlots(slots, entity.modules, ::sendOpenModule, 0, moduleCount, 45, 0)

                inventoryPanel.add(slots, 0, 18, 9 * 18, 2 * 18)

                permissionsButton?.let { inventoryPanel.add(it, 8 * 18, 3 * 18, 18, 18) }
            }
            3 -> {
                inventoryPanel.add(createPlayerInventoryPanel(), 0, 6 * 18 + 9)

                val slots = WPlainPanel()
                addSlots(slots, entity.modules, ::sendOpenModule, 0, moduleCount / 2, 9, 0)
                addSlots(slots, entity.modules, ::sendOpenModule, moduleCount / 2, moduleCount / 2, 9, 18 * 3)

                inventoryPanel.add(slots, 0, 18, 9 * 18, 5 * 18)

                permissionsButton?.let { inventoryPanel.add(it, 8 * 18, 6 * 18, 18, 18) }
            }
            else -> LOGGER.warn("Opened GUI for transpositioner with invalid mk: ${entity.mk}")
        }

        root.add(0, inventoryPanel)

        // permissions panel

        if (editPermission) {
            val permissionsPanel = WPlainPanel()
            permissionsPanel.insets = Insets.ROOT_PANEL

            val backButton = WScalableButton(LiteralText("<-"))
            backButton.onClick = {
                root.selectedIndex = 0
            }
            permissionsPanel.add(backButton, 0, 0, 18, 18)

            val ownerText = WText(gui("owner", ""))
            this.ownerText = ownerText

            val playerTextField = WSpecialTextField()
            this.playerTextField = playerTextField

            val addPlayerButton = WScalableButton(icon = IconUtils.CHECK_ICON)
            val removePlayerButton = WScalableButton(icon = IconUtils.X_ICON)
            this.addPlayerButton = addPlayerButton
            this.removePlayerButton = removePlayerButton
            addPlayerButton.enabled = false
            removePlayerButton.enabled = false
            addPlayerButton.tooltip = listOf(tooltipLine(tooltip("add_player")))
            removePlayerButton.tooltip = listOf(tooltipLine(tooltip("remove_player")))

            addPlayerButton.onClick = {
                val playerName = playerTextField.text
                if (!playerSet.contains(playerName.lowercase())) {
                    ID_ADD_PLAYER.sendToServer {
                        it.writeString(playerName)
                    }
                }
            }

            removePlayerButton.onClick = {
                val playerName = playerTextField.text
                if (playerSet.contains(playerName.lowercase())) {
                    ID_REMOVE_PLAYER.sendToServer {
                        it.writeString(playerName)
                    }
                }
            }

            playerTextField.onChanged = { playerName ->
                val contains = playerSet.contains(playerName.lowercase())
                addPlayerButton.enabled = !contains && playerName.isNotBlank()
                removePlayerButton.enabled = contains
            }

            val playerListWidget = WListPanel(playerList, { WScalableButton() }, { playerName, button ->
                button.label = LiteralText(playerName)
                button.setSize(5 * 18, 18)
                button.onClick = {
                    playerTextField.setText(playerName)
                    addPlayerButton.enabled = false
                    removePlayerButton.enabled = true
                }
            })
            this.playerListWidget = playerListWidget
            playerListWidget.backgroundPainter = BackgroundPainter.SLOT

            val lockedButton = WScalableButton(icon = if (entity.isLocked()) IconUtils.LOCKED else IconUtils.UNLOCKED)
            this.lockedButton = lockedButton
            lockedButton.tooltip =
                listOf(tooltipLine(lockedState(entity.isLocked())), tooltipLine(lockedExtra(entity.isLocked())))
            lockedButton.onClick = {
                val newLocked = !entity.isLocked()
                ID_LOCKED_CHANGE.sendToServer {
                    it.writeBoolean(newLocked)
                }
            }

            val verticalOffset = when (entity.mk) {
                1 -> 9
                2 -> 9
                3 -> 2 * 18
                else -> 0
            }

            permissionsPanel.add(ownerText, 2 * 18, verticalOffset, 7 * 18, 18)
            permissionsPanel.add(playerListWidget, 2 * 18, 18 + verticalOffset, 5 * 18, 5 * 18)
            permissionsPanel.add(playerTextField, 18, 6 * 18 + verticalOffset, 5 * 18, 18)
            permissionsPanel.add(addPlayerButton, 6 * 18, 6 * 18 + verticalOffset, 18, 18)
            permissionsPanel.add(removePlayerButton, 7 * 18, 6 * 18 + verticalOffset, 18, 18)
            permissionsPanel.add(lockedButton, 3 * 18, 7 * 18 + verticalOffset, 3 * 18, 18)

            root.add(1, permissionsPanel)
        }

        root.validate(this)

        if (!world.isClient && editPermission) {
            sendPlayerList()
        }
    }

    private fun sendOpenModule(index: Int) {
        CommonProxy.INSTANCE.presetCursorPosition()
        ID_OPEN_MODULE.sendToServer {
            it.writeVarInt(index)
        }
    }

    private fun sendPlayerList() {
        val world = world as ServerWorld
        val userCache = world.server.userCache
        val owner = entity.permissions.owner?.let { userCache.getByUuid(it).orNull() }?.name

        val playerList = mutableListOf<String>()
        for (playerUuid in entity.permissions.playerSet) {
            playerList.add(userCache.getByUuid(playerUuid).orNull()?.name ?: playerUuid.toString().replace("-", ""))
        }

        playerList.sortBy { it.lowercase() }
        this.playerList.clear()
        this.playerList.addAll(playerList)

        ID_PLAYER_LIST.sendToClient {
            if (owner == null) {
                it.writeBoolean(false)
            } else {
                it.writeBoolean(true)
                it.writeString(owner)
            }

            it.writeVarUnsignedInt(playerList.size)
            for (player in playerList) {
                it.writeString(player)
            }
        }
    }

    private fun forPlayer(playerString: String, forPlayer: (UUID) -> Unit) {
//        try {
//            val uuid = UUID.fromString(playerString)
//            forPlayer(uuid)
//        } catch (_: IllegalArgumentException) {
        world.server!!.userCache.findByNameAsync(playerString) { optional ->
            optional.ifPresent { profile ->
                forPlayer(profile.id)
                sendPlayerList()
            }
        }
//        }
    }

    fun s2cReceiveLockedChange(locked: Boolean) {
        val lockedButton = lockedButton ?: return
        lockedButton.icon = if (locked) IconUtils.LOCKED else IconUtils.UNLOCKED
        lockedButton.tooltip = listOf(tooltipLine(lockedState(locked)), tooltipLine(lockedExtra(locked)))
    }
}