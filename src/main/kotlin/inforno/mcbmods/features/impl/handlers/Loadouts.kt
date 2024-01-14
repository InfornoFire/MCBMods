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

import gg.essential.universal.UChat
import inforno.mcbmods.MCBMods
import inforno.mcbmods.MCBModsKt
import inforno.mcbmods.core.PersistentSave
import inforno.mcbmods.gui.LoadoutTimerGui
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.item.Item
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.commons.codec.binary.Base64InputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.concurrent.atomic.AtomicInteger


object Loadouts : PersistentSave(File(MCBModsKt.configDir, "loadouts.json")) {

    const val LOADOUTS_SIZE = 10
    val loadouts = MutableList(size = LOADOUTS_SIZE, init = { Loadout(emptySet()) })

    private const val LOADOUT_DELAY = 7
    private val time = AtomicInteger(0)
    private var delay = false

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

    fun load(slotIndex: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            delay(LOADOUT_DELAY * 1000L)
            loadouts[slotIndex].loadoutItems.forEach {
                val item = GameRegistry.findItem(it.id.substringBefore(":"), it.id.substringAfter(":"))
                val itemID = Item.itemRegistry.getIDForObject(item)
                UChat.say("/shop buy $itemID:${it.dmg} ${it.count}")
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            delay = true
            time.set(LOADOUT_DELAY)
            repeat(LOADOUT_DELAY) {
                delay(1000L)
                time.set(time.get() - 1)
            }
            delay = false
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Text){
        if (delay) {
            LoadoutTimerGui(mc, event.resolution, time.get())
        }
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