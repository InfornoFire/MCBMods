/*
 * MCBMods
 * Copyright (C) 2018-2024 Inforno
 *
 * This file is a derivative work based on Waypoints.kt from Skytils
 * https://github.com/Skytils/SkytilsMod/blob/2432c16195abea612ef2d3153a6c1c59246c9ea7/src/main/kotlin/gg/skytils/skytilsmod/features/impl/handlers/Waypoints.kt
 *
 * Original work:
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package inforno.mcbmods.features.impl.handlers

import inforno.mcbmods.MCBMods
import inforno.mcbmods.MCBModsKt
import inforno.mcbmods.core.PersistentSave
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.apache.commons.codec.binary.Base64InputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.io.Reader
import java.io.Writer

object Loadouts : PersistentSave(File(MCBModsKt.configDir, "loadouts.json")) {

    const val LOADOUTS_SIZE = 10
    val loadouts = MutableList(size = LOADOUTS_SIZE, init = { Loadout(emptySet()) })

    fun getLoadoutFromString(str: String): Loadout? {
        if (str.startsWith("[MCBMods-Loadout]:")) {
            val version = str.substringAfter(':').substringBefore('-').toIntOrNull() ?: 0
            val content = str.substringAfter('>')

            val data = when (version) {
                1 -> {
                    GzipCompressorInputStream(Base64InputStream(content.byteInputStream())).use {
                        it.readBytes().decodeToString()
                    }
                }
                else -> throw IllegalArgumentException("Unknown version $version")
            }

            return json.decodeFromString<Loadout>(data)
        }
        return null
    }

    override fun read(reader: Reader) {
        val str = reader.readText()
        runCatching {
            loadouts.clear()
            loadouts.addAll(json.decodeFromString<LoadoutList>(str).loadouts)
        }.onFailure {
            MCBMods.LOGGER.info("Could not read loadouts save")
            // If format changes we can handle it here
        }
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(LoadoutList(loadouts)))
    }

    override fun setDefault(writer: Writer) {
        writer.write(json.encodeToString(LoadoutList(emptyList())))
    }
}

@Serializable
data class LoadoutList(
    val loadouts: List<Loadout>
)

@Serializable
data class Loadout(
    val loadoutItems: Set<LoadoutItem>,
)

@Serializable
data class LoadoutItem(
    val id: String,
    val dmg: Int,
    val count: Int,
    val slot: Int
)