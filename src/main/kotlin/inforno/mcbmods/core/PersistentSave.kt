/*
 * MCBMods
 * Copyright (C) 2018-2024 Inforno
 *
 * This file is a derivative work based on PersistentSave.kt from Skytils
 * https://github.com/Skytils/SkytilsMod/blob/2432c16195abea612ef2d3153a6c1c59246c9ea7/src/main/kotlin/gg/skytils/skytilsmod/core/PersistentSave.kt
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

package inforno.mcbmods.core

import inforno.mcbmods.MCBMods
import inforno.mcbmods.MCBModsKt
import kotlinx.serialization.json.Json
import net.minecraft.client.Minecraft
import java.io.File
import java.io.Reader
import java.io.Writer

abstract class PersistentSave(private val saveFile: File) {

    protected val json: Json = MCBModsKt.json

    protected val mc: Minecraft = MCBMods.mc

    abstract fun read(reader: Reader)

    abstract fun write(writer: Writer)

    abstract fun setDefault(writer: Writer)

    fun readSave() {
        try {
            saveFile.ensureFile()
            saveFile.bufferedReader().use {
                read(it)
            }
        } catch (e: Exception) {
            MCBMods.LOGGER.info("Could not find save file for ${saveFile.name}, attempting to create default file...")
            try {
                saveFile.bufferedWriter().use {
                    setDefault(it)
                    MCBMods.LOGGER.info("Successfully created the default file")
                }
            } catch (ex: Exception) {
                MCBMods.LOGGER.error("Failed to create default save file:", ex)
            }
        }
    }

    fun writeSave() {
        try {
            saveFile.ensureFile()
            saveFile.writer().use { writer ->
                write(writer)
            }
        } catch (ex: Exception) {
            MCBMods.LOGGER.error("Failed to create save file:", ex)
        }
    }
}

fun File.ensureFile() = (parentFile.exists() || parentFile.mkdirs()) && createNewFile()