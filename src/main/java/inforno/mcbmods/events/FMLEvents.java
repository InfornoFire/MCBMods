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

package inforno.mcbmods.events;

import gg.essential.universal.UChat;
import inforno.mcbmods.MCBMods;
import inforno.mcbmods.commands.AfkCommand;
import inforno.mcbmods.config.MCBModsConfig;
import inforno.mcbmods.keybinds.KeyBinds;
import journeymap.client.JourneymapClient;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import static inforno.mcbmods.MCBMods.mc;

public class FMLEvents {

    public static boolean afkchat;
    private long prevTime, prevTime2;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player != null) {
            if (AfkCommand.afk) {
                if (Display.isActive() && (player.moveForward != 0 || player.moveStrafing != 0)) {
                    AfkCommand.afk = false;
                    UChat.chat(MCBMods.prefix + "§bYou are no longer afk.");
                }
            }

            if (!afkchat && System.currentTimeMillis() - prevTime > 5000) {
                afkchat = true;
                prevTime = System.currentTimeMillis();
            }

            if (MCBModsConfig.autoAFK && !AfkCommand.afk && !Display.isActive() && System.currentTimeMillis() - prevTime2 > 3000) {
                AfkCommand.afk = true;
                UChat.chat(MCBMods.prefix + "§bYou are now afk.");
                prevTime2 = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onEvent(KeyInputEvent event) {
        if (KeyBinds.getWaypointToggle().isPressed()) {
            JourneymapClient.getWaypointProperties().maxDistance.set(JourneymapClient.getWaypointProperties().maxDistance.get() == 0 ? 1 : 0);
            if (MCBModsConfig.deathpointToggler) {
                JourneymapClient.getWaypointProperties().createDeathpoints.set(JourneymapClient.getWaypointProperties().maxDistance.get() == 0);
            }
        }
    }
}
