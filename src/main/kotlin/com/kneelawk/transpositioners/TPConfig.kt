package com.kneelawk.transpositioners

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer

class TPConfig {
    companion object {
        private val configFile = FabricLoader.getInstance().configDir.resolve("transpositioners.json")

        private fun load(): TPConfig {
            val gson = GsonBuilder().setLenient().create()

            return try {
                val reader = configFile.reader()

                gson.fromJson(reader, TPConfig::class.java)
            } catch (_: Exception) {
                val config = TPConfig()

                if (!configFile.parent.exists()) {
                    configFile.parent.createDirectories()
                }

                gson.toJson(config, configFile.writer())

                config
            }
        }

        val CONFIG by lazy { load() }
    }

    var defaultLocked = false
}
