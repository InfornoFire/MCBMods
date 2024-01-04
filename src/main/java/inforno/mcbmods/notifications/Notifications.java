package inforno.mcbmods.notifications;

import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.net.MalformedURLException;

public class Notifications {

    public static boolean focused;

    public static void macNotification(String title, String message) {
        Runtime runtime = Runtime.getRuntime();
        String[] args = {"osascript", "-e", "display notification \"" + message + "\" with title \"" + title + "\""};
        try {
            Process process = runtime.exec(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void windowsNotification(String title, String message) throws AWTException, MalformedURLException {
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else if (Minecraft.isRunningOnMac) {
            macNotification(title, message);
        }
    }
}
