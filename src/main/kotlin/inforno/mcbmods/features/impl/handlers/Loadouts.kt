/*
 * MCBMods
 * Copyright (C) 2018-2024 Inforno
 *
 * This file contains derivative code based on Waypoints.kt from Skytils
 * https://github.com/Skytils/SkytilsMod/blob/2432c16195abea612ef2d3153a6c1c59246c9ea7/src/main/kotlin/gg/skytils/skytilsmod/features/impl/handlers/Waypoints.kt
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
 *
 * This file contains derivative code based on DirectContainerManager.java from Jimeo Wan and Inventory Tweaks
 * https://github.com/Inventory-Tweaks/inventory-tweaks/blob/c6b9882c5f2c332e376832d2097e54a99ef99911/src/main/java/invtweaks/container/DirectContainerManager.java
 * Inventory Tweaks (Kobata)
 * Copyright (c) 2011-2013 Marwane Kalam-Alami
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
import inforno.mcbmods.gui.LoadoutMessageGui
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
    private var state = State.IDLE

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
            delay(500L)
            state = State.SHOP_WAITING
            delay(2500L)
            state = State.SORTING
            sort(slotIndex)
            delay(500L)
            state = State.IDLE
        }

        CoroutineScope(Dispatchers.Default).launch {
            state = State.LOADOUT_DELAY
            time.set(LOADOUT_DELAY)
            repeat(LOADOUT_DELAY) {
                delay(1000L)
                time.set(time.get() - 1)
            }
            state = State.IDLE
        }
    }

    private fun sort(slotIndex: Int) {
        putHeldItemDown()
        loadouts[slotIndex].loadoutItems.forEach { loadoutItem ->
            val targetSlot = mc.thePlayer.inventoryContainer.inventorySlots.find {
                val item = it.stack?.item
                item?.registryName == loadoutItem.id && item.getDamage(it.stack) == loadoutItem.dmg
            }
            targetSlot?.let { slot ->
                swapItems(slot.slotNumber, loadoutItem.slot)
            }
        }
        putHeldItemDown()
    }

    private fun swapItems(srcSlot: Int, destSlot: Int) {
        if (srcSlot == destSlot) return
        val playerController = mc.playerController
        val container = mc.thePlayer.inventoryContainer
        val emptyDest = container.inventorySlots[destSlot].stack == null
        playerController.windowClick(container.windowId, srcSlot, 0, 0, mc.thePlayer)
        playerController.windowClick(container.windowId, destSlot, 0, 0, mc.thePlayer)
        if (!emptyDest) {
            playerController.windowClick(container.windowId, srcSlot, 0, 0, mc.thePlayer)
        }
    }

    private fun putHeldItemDown() {
        val targetSlot = mc.thePlayer.inventoryContainer.inventorySlots.find { !it.hasStack && it.slotNumber > 4 }
        targetSlot?.let { slot ->
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot.slotNumber, 0, 0, mc.thePlayer)
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Text) {
        when (state) {
            State.IDLE -> return
            State.LOADOUT_DELAY -> LoadoutMessageGui(mc, event.resolution, "Loadout loading in ${time.get()}")
            State.SHOP_WAITING -> LoadoutMessageGui(mc, event.resolution, "Waiting for purchase...")
            State.SORTING -> LoadoutMessageGui(mc, event.resolution, "Sorting items...")
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

enum class State {
    IDLE,
    LOADOUT_DELAY,
    SHOP_WAITING,
    SORTING
}