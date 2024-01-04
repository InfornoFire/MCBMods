package inforno.mcbmods.events;

import inforno.mcbmods.MCBMods;
import inforno.mcbmods.commands.AfkCommand;
import inforno.mcbmods.config.MCBModsConfig;
import inforno.mcbmods.keybinds.KeyBinds;

import gg.essential.universal.UChat;
import journeymap.client.JourneymapClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.opengl.Display;

public class FMLEvents {

    public static boolean afkchat;
    private long prevTime, prevTime2;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (MCBMods.player.get() != null) {
            if (AfkCommand.afk) {
                if (Display.isActive() && (MCBMods.player.get().moveForward != 0 || MCBMods.player.get().moveStrafing != 0)) {
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
