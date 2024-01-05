package inforno.mcbmods.keybinds;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBinds {

    private static final KeyBinding waypointtoggle = new KeyBinding("Waypoint Toggler", Keyboard.KEY_L, "MCBMods");

    public static void register() {
        ClientRegistry.registerKeyBinding(waypointtoggle);
    }

    public static KeyBinding getWaypointToggle() {
        return waypointtoggle;
    }
    
}
