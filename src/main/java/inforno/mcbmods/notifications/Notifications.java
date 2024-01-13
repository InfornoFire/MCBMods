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
