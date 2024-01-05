package inforno.mcbmods.config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import inforno.mcbmods.MCBMods;

import java.io.File;
import java.util.HashMap;

public class MCBModsConfig extends Vigilant {

    @Property(
            type = PropertyType.SWITCH, name = "Auto AFK",
            description = "Turns afk mode on when display is inactive",
            category = "General", subcategory = "Afk"
    )
    public static boolean autoAFK = false;

    @Property(
            type = PropertyType.TEXT, name = "AFK Message",
            description = "Changes the reply message when someone messages you during afk",
            category = "General", subcategory = "Afk"
    )
    public static String afkMessage = "I am afk!";

    @Property(
            type = PropertyType.SWITCH, name = "Notify Mission",
            description = "Notifies the system of an incoming mission",
            category = "General", subcategory = "Notifications"
    )
    public static boolean notyifyMission = false;

    @Property(
            type = PropertyType.SWITCH, name = "Notify Name",
            description = "Notifies the system when the player's name is in chat",
            category = "General", subcategory = "Notifications"
    )
    public static boolean notifyName = false;

    @Property(
            type = PropertyType.SWITCH, name = "Notify PM",
            description = "Notifies the system when the player receives a PM",
            category = "General", subcategory = "Notifications"
    )
    public static boolean notifyPM = false;

    @Property(
            type = PropertyType.SWITCH, name = "Waypoint Toggler Deathpoints",
            description = "Should Waypoint Toggler keybind control deathpoints?",
            category = "General", subcategory = "Misc"
    )
    public static boolean deathpointToggler = false;

    @Property(
            type = PropertyType.SWITCH, name = "Load Shop Data",
            description = "Should Shop Data be loaded? Applies only to game launch",
            category = "General", subcategory = "Shop Data"
    )
    public static boolean loadShopData = true;

    @Property(
            type = PropertyType.BUTTON, name = "Reload Shop Data",
            description = "Reloads/Unloads all Shop Data\n" +
                    "Only useful after toggling Load Shop Data or if data did not load",
            category = "General", subcategory = "Shop Data",
            placeholder = "Reload"
    )
    public static void reloadShopData() {
        MCBMods.shopData = new HashMap<>();
        if (loadShopData) MCBMods.loadShopData();
    }

    @Property(
            type = PropertyType.SWITCH, name = "Display Shop Data",
            description = "Should Shop Data be displayed in item descriptions?",
            category = "General", subcategory = "Shop Data"
    )
    public static boolean displayShopData = true;

    @Property(
            type = PropertyType.SWITCH, name = "Display Chest Worth",
            description = "Calculate and display chest worth??",
            category = "General", subcategory = "Shop Data"
    )
    public static boolean chestWorth = true;

    @Property(
            type = PropertyType.SWITCH, name = "Fix Vanilla Armor Value Display",
            description = "The visible armor value by Flans Armor is out of 100%% but Vanilla Armor is out of 80%%, leading to confusion between the two systems",
            category = "General", subcategory = "Misc"
    )
    public static boolean fixArmorDisplay = true;

    @Property(
            type = PropertyType.SWITCH, name = "Display Gun Data",
            description = "Should Gun Data be displayed in item descriptions?",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displayGunData = true;

    @Property(
            type = PropertyType.SWITCH, name = "Damage",
            description = "Show Damage",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displayDamage = true;

    @Property(
            type = PropertyType.SWITCH, name = "All Ammo",
            description = "Show All Ammo Damage",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean allAmmoDamage = false;

    @Property(
            type = PropertyType.SWITCH, name = "Spread",
            description = "Show Spread",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displaySpread = true;

    @Property(
            type = PropertyType.SWITCH, name = "ROF",
            description = "Show Rate of Fire",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displayROF = true;

    @Property(
            type = PropertyType.SWITCH, name = "Recoil",
            description = "Show Recoil",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displayRecoil = true;

    @Property(
            type = PropertyType.SWITCH, name = "Switch Delay",
            description = "Show Switch Delay",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displaySwitchDelay = true;

    @Property(
            type = PropertyType.SWITCH, name = "Paintjob",
            description = "Show Paintjob",
            category = "Gun Data", subcategory = "Item Description"
    )
    public static boolean displayPaintjob = true;

    public static MCBModsConfig INSTANCE = new MCBModsConfig();

    public MCBModsConfig() {
        super(new File("./config/mcbmods.toml"), "MCBMods (" + MCBMods.VERSION + ")");
        initialize();

        try {
            addDependency("displayShopData", "loadShopData");
            addDependency("chestWorth", "loadShopData");

            addDependency("displayDamage", "displayGunData");
            addDependency("allAmmoDamage", "displayDamage");
            addDependency("displaySpread", "displayGunData");
            addDependency("displayROF", "displayGunData");
            addDependency("displayRecoil", "displayGunData");
            addDependency("displaySwitchDelay", "displayGunData");
            addDependency("displayPaintjob", "displayGunData");
        } catch (Exception e) {
            MCBMods.LOGGER.error("Failed to access config properties:", e);
        }
    }
}
