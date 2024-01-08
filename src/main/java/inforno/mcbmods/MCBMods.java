package inforno.mcbmods;

import gg.essential.api.EssentialAPI;
import gg.essential.api.commands.Command;
import gg.essential.universal.UDesktop;
import inforno.mcbmods.commands.*;
import inforno.mcbmods.config.MCBModsConfig;
import inforno.mcbmods.events.Events;
import inforno.mcbmods.events.FMLEvents;
import inforno.mcbmods.features.impl.handlers.Loadouts;
import inforno.mcbmods.keybinds.KeyBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

@Mod(name = MCBMods.NAME, version = MCBMods.VERSION, clientSideOnly = true, modid = MCBMods.MODID,
        dependencies = "required-after:mcbClient;required-after:journeymap;required-after:flansmod",
        acceptedMinecraftVersions = "[1.8.9]")
public class MCBMods {

    @Mod.Instance("mcbmods")
    public static MCBMods instance;

    public static final String NAME = "MCBMods";
    public static final String VERSION = "1.4.3";
    public static final String MODID = "mcbmods";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static String prefix = "§c[§6MCBMods§c]§r ";

    public static String latestVersion;
    public static String latestVersionLink;

    public static Minecraft mc = Minecraft.getMinecraft();

    public static MCBModsConfig config;

    public static HashMap<Integer, float[]> shopData = new HashMap<>();

    public static Class<?> customEnderChest;

    @EventHandler
    public void preinit(FMLPreInitializationEvent e) {
        KeyBinds.register();
        MinecraftForge.EVENT_BUS.register(new Events());
        MinecraftForge.EVENT_BUS.register(new FMLEvents());
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        config = MCBModsConfig.INSTANCE;

        registerCommands(
                new MCBModsCommand(),
                new AfkCommand(),
                new ChatCommand(),
                new InvWorthCommand(),
                new DamageCalcCommand(),
                new LoadoutCommand()
        );
    }

    @EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        try {
            Class<?> testedClass = Class.forName("com.mcb.client.c.d");
            if (GuiContainer.class.isAssignableFrom(testedClass)) {
                customEnderChest = testedClass;
            }
        } catch (Exception e) {
            LOGGER.error("MCB Client dependency error:", e);
        }

        if (MCBModsConfig.loadShopData) loadShopData();

        Loadouts.INSTANCE.readSave();

        if (versionChecker() > 0) {
            EssentialAPI.getNotifications().push("MCBMods", "New Version available! Click to update to " + latestVersion, () -> {
                UDesktop.browse(URI.create(latestVersionLink));
                return null;
            });
        }
    }

    private void registerCommands(Command... commands) {
        for (Command command : commands) {
            EssentialAPI.getCommandRegistry().registerCommand(command);
        }
    }

    public static int versionChecker() {
        try {
            URL version = new URL("https://raw.githubusercontent.com/InfornoFire/MCBMods-Data/master/Version.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(version.openStream()));
            latestVersion = br.readLine();
            latestVersionLink = br.readLine();
            return new ComparableVersion(latestVersion).compareTo(new ComparableVersion(MCBMods.VERSION));
        } catch (IOException e) {
            LOGGER.warn("Could not check version:", e);
        }
        return 0;
    }

    public static void loadShopData() {
        String line;
        String splitBy = ",";
        try {
            // BufferedReader br = new BufferedReader(new FileReader("./config/mcbmods/shopdata.csv"));
            URL url = new URL("https://raw.githubusercontent.com/InfornoFire/MCBMods-Data/master/shopdata.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = br.readLine()) != null) {
                String[] data = line.split(splitBy);
                float[] val = {Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])};
                shopData.put(data[0].hashCode(), val);
            }
        } catch (IOException e) {
            EssentialAPI.getNotifications().push("MCBMods", "Failed to Load Shop Data!");
            LOGGER.warn("Failed to Load Shop Data:", e);
        }
    }

    /**
     * Accepts an ItemStack formatted returns the Shop Data values associated <br>
     * If no value was found with the ItemStack's metadata value, the metadata 0 will be used instead <br>
     * The return index values are: <br>
     * 0: Buy Price <br>
     * 1: Sell Price, modified by Durability <br>
     * 2: Level <br>
     */
    public static float @Nullable [] getWorth(ItemStack stack) {
        if (stack == null) return null;
        float[] data;
        if ((data = MCBMods.shopData.get((stack.getItem().getRegistryName() + ":" + stack.getItem().getDamage(stack)).hashCode())) == null) {
            if ((data = MCBMods.shopData.get((stack.getItem().getRegistryName() + ":0").hashCode())) == null)
                return null;
        }
        float[] copy = data.clone();
        if (stack.getItem().isDamageable()) copy[1] *= (float) (1.0 - stack.getItem().getDurabilityForDisplay(stack));
        return copy;
    }
}
