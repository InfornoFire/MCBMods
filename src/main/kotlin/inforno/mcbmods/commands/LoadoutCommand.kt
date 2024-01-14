/*
 * MCBMods
 * Copyright (C) 2018-2024 Inforno
 *
 * Portions of this file are a derivative work based on WaypointShareGui.kt from Skytils
 * https://github.com/Skytils/SkytilsMod/blob/2432c16195abea612ef2d3153a6c1c59246c9ea7/src/main/kotlin/gg/skytils/skytilsmod/gui/WaypointShareGui.kt
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

package inforno.mcbmods.commands

import gg.essential.api.commands.*
import gg.essential.universal.UChat
import gg.essential.universal.UDesktop
import inforno.mcbmods.MCBMods
import inforno.mcbmods.MCBMods.mc
import inforno.mcbmods.MCBModsKt
import inforno.mcbmods.features.impl.handlers.Loadout
import inforno.mcbmods.features.impl.handlers.LoadoutItem
import inforno.mcbmods.features.impl.handlers.Loadouts
import kotlinx.serialization.encodeToString
import org.apache.commons.codec.binary.Base64
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import org.apache.commons.io.output.ByteArrayOutputStream
import java.util.zip.Deflater

class LoadoutCommand: Command("loadout") {

    @DefaultHandler
    fun handle() {
        UChat.chat( "${MCBMods.prefix} Use /loadout save <slot> <type>, /loadout load <slot>, or /loadout export <slot>")
    }

    @SubCommand(value = "save", description = "Creates a loadout save")
    fun save(@DisplayName("slot") slot: Int, @DisplayName("type") @Options(["inventory", "clipboard"]) type: String) {
        val index = slot.validate()
        if (index < 0) return

        when (type) {
            "inventory" -> {
                val player = mc.thePlayer
                val inventory = player.inventory
                val itemsData = hashSetOf<LoadoutItem>()

                for (itemSlot in 0 until inventory.sizeInventory) {
                    val itemStack = inventory.getStackInSlot(itemSlot)
                    itemStack?.let {
                        itemsData.add(LoadoutItem(
                            id = itemStack.item.registryName,
                            dmg = if (!itemStack.item.isDamageable) itemStack.item.getDamage(itemStack) else 0,
                            count = itemStack.stackSize,
                            slot = itemSlot
                        ))
                    }
                }

                Loadouts.loadouts[index] = Loadout(itemsData)
            }
            "clipboard" -> {
                val str = UDesktop.getClipboardString()
                Loadouts.getLoadoutFromString(str)?.let { Loadouts.loadouts[index] = it }
            }
            else -> {
                UChat.chat("${MCBMods.prefix}Error invalid save type")
            }
        }
        UChat.chat("${MCBMods.prefix}Loadout slot $slot successfully created")
        Loadouts.writeSave()
    }

    @SubCommand(value = "load", description = "Loads a loadout and buys/sort all the items")
    fun load(@DisplayName("slot") slot: Int) {
        val index = slot.validate()
        if (index < 0) return
        if(!mc.thePlayer.inventory.armorInventory.all { it == null }
                || !mc.thePlayer.inventory.mainInventory.all { it == null }) {
            UChat.chat("${MCBMods.prefix}Make sure your inventory is clear!")
        }

        Loadouts.load(index)
    }

    @SubCommand(value = "export", description = "Export a loadout to clipboard")
    fun export(@DisplayName("slot") slot: Int) {
        val index = slot.validate()
        if (index < 0) return

        val str = MCBModsKt.json.encodeToString(Loadouts.loadouts[index])
        val data = Base64.encodeBase64String(ByteArrayOutputStream().use { baos ->
            GzipCompressorOutputStream(baos, GzipParameters().apply {
                compressionLevel = Deflater.BEST_COMPRESSION
            }).use { gs ->
                gs.write(str.encodeToByteArray())
            }
            baos.toByteArray()
        })

        UDesktop.setClipboardString("[MCBMods-Loadout]:1->${data}")
        UChat.chat("${MCBMods.prefix}Loadout slot $slot successfully copied to clipboard")
    }
}

fun Int.validate(): Int {
    val decremented = this - 1
    return if (decremented in 0..<Loadouts.LOADOUTS_SIZE) {
        decremented
    } else {
        UChat.chat("${MCBMods.prefix}Slot must be between 1-10")
        -1
    }
}