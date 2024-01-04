package inforno.mcbmods.keybinds;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBinds {

    private static KeyBinding waypointtoggle = new KeyBinding("Waypoint Toggler", Keyboard.KEY_L, "MCBMods");

    public static final void register() {
        ClientRegistry.registerKeyBinding(waypointtoggle);
    }

    public static KeyBinding getWaypointToggle() {
        return waypointtoggle;
    }
    
}
