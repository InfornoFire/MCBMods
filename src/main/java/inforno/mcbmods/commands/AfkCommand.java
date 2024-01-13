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

package inforno.mcbmods.commands;

import inforno.mcbmods.MCBMods;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.universal.UChat;

public class AfkCommand extends Command {

    public static boolean afk;

    public AfkCommand() {
        super("afk");
    }

    @DefaultHandler
    public void handle() {
        if (!afk) {
            AfkCommand.afk = true;
            UChat.chat(MCBMods.prefix + "§bYou are now afk.");
        } else {
            AfkCommand.afk = false;
            UChat.chat(MCBMods.prefix + "§bYou are no longer afk.");
        }
    }
}
