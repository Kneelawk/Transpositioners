package com.kneelawk.transpositioners.util

import com.kneelawk.transpositioners.TPConfig
import com.kneelawk.transpositioners.TPConstants.message
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.time.Duration
import java.time.Instant
import java.util.*

class PermissionManager(var owner: UUID?, var locked: Boolean, playerSet: Set<UUID>, var listAllow: Boolean) {
    companion object {
        private val ERROR_DELAY = Duration.ofSeconds(1)

        fun fromNBT(tag: NbtCompound): PermissionManager {
            val owner = if (tag.contains("owner")) tag.getUuid("owner") else null
            val locked = tag.getBoolean("locked")
            val playerList =
                tag.getList("playerList", NbtType.COMPOUND).asSequence().map { (it as NbtCompound).getUuid("uuid") }
                    .toSet()
            val listAllow = tag.getBoolean("listAllow")
            return PermissionManager(owner, locked, playerList, listAllow)
        }

        fun newDefault(owner: PlayerEntity?): PermissionManager {
            val ownerUUID = owner?.uuid
            val locked = TPConfig.CONFIG.defaultLocked
            val playerList = setOf<UUID>()
            val listAllow = true
            return PermissionManager(ownerUUID, locked, playerList, listAllow)
        }
    }

    private val erroredPlayers = mutableMapOf<UUID, Instant>()

    private val playerSetMut = playerSet.toMutableSet()
    val playerSet: Set<UUID>
        get() = playerSetMut

    fun toNBT(): NbtCompound {
        val tag = NbtCompound()
        owner?.let { tag.putUuid("owner", it) }
        tag.putBoolean("locked", locked)

        tag.put("playerList", playerSet.fold(NbtList()) { list, player ->
            val playerTag = NbtCompound()
            playerTag.putUuid("uuid", player)
            list.add(playerTag)
            list
        })

        tag.putBoolean("listAllow", listAllow)

        return tag
    }

    fun addPlayer(uuid: UUID) {
        playerSetMut.add(uuid)
    }

    fun removePlayer(uuid: UUID) {
        playerSetMut.remove(uuid)
    }

    private fun ServerPlayerEntity.trySendMessage(message: Text) {
        // Sometimes the sendPermissionError method gets called multiple times for a single click, so this is used to
        // prevent it from spamming the player who just clicked.
        val now = Instant.now()
        val cutoff = now.minus(ERROR_DELAY)
        erroredPlayers.removeAll { _, instant -> instant.isBefore(cutoff) }

        if (!erroredPlayers.contains(uuid)) {
            erroredPlayers[uuid] = now
            sendMessage(message, false)
        }
    }

    fun sendPermissionError(player: ServerPlayerEntity) {
        val owner = owner
        if (owner != null) {
            val server = player.server
            val ownerName = server.userCache.getByUuid(owner).orNull()?.name ?: "an operator"
            player.trySendMessage(message("error.permission.player", LiteralText(ownerName).formatted(Formatting.BLUE)))
        } else {
            player.trySendMessage(message("error.permission.unknown"))
        }
    }

    fun canEditPermissions(player: PlayerEntity?): Boolean {
        // non-players cannot edit permissions
        if (player == null) {
            return false
        }

        // only ops and the owner can edit permissions

        if (player.uuid == owner) {
            return true
        }

        return player.hasPermissionLevel(2)
    }

    fun hasPermission(player: PlayerEntity?): Boolean {
        // being unlocked means anyone can access
        if (!locked) {
            return true
        }

        // non-players can only interact with unlocked transpositioners for the time-being
        if (player == null) {
            return false
        }

        // always let the owner access
        if (player.uuid == owner) {
            return true
        }

        // always let ops access
        if (player.hasPermissionLevel(2)) {
            return true
        }

        // if listAllow, then people in the player list are allowed
        if (playerSet.contains(player.uuid)) {
            return listAllow
        }

        // if !listAllow, then people in the player list are excluded
        return !listAllow
    }

    override fun toString(): String {
        return "PermissionManager(owner=$owner, locked=$locked, listAllow=$listAllow, playerSet=$playerSet)"
    }
}
