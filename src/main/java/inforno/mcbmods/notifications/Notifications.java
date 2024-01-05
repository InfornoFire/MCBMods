package inforno.mcbmods.notifications;

import inforno.mcbmods.MCBMods;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;

public class Notifications {

    public static void macNotification(String title, String message) {
        Runtime runtime = Runtime.getRuntime();
        String[] args = {"osascript", "-e", "display notification \"" + message + "\" with title \"" + title + "\""};
        try {
            runtime.exec(args);
        } catch (IOException e) {
            MCBMods.LOGGER.error("Error in Mac notification:", e);
        }
    }

    public static void windowsNotification(String title, String message) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "notification");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("notification");
        tray.add(trayIcon);
        trayIcon.displayMessage(title, message, MessageType.NONE);
    }

    public static void notify(String title, String message) {
        if (!Minecraft.isRunningOnMac) {
            try {
                windowsNotification(title, message);
            } catch (Exception e) {
                MCBMods.LOGGER.error("Error in Windows notification:", e);
            }
        } else {
            macNotification(title, message);
        }
    }
}
