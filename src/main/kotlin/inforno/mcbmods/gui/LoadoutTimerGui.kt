/*
 * MCBMods
 * Copyright (C) 2018-2024 Inforno
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

package inforno.mcbmods.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

class LoadoutTimerGui(
    mc: Minecraft,
    scaledResolution: ScaledResolution,
    time: Int
) : Gui() {
    init {
        val text = "Loadout loading in $time"
        val textX = (scaledResolution.scaledWidth - mc.fontRendererObj.getStringWidth(text)) / 2f
        val textY = scaledResolution.scaledHeight - 59f
        GlStateManager.pushMatrix()
        mc.fontRendererObj.drawString(text, textX, textY, 0xFF9A00, false)
        GlStateManager.popMatrix()
    }
}