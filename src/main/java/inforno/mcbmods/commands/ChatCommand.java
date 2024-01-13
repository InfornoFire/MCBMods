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

import com.google.common.collect.ImmutableSet;
import gg.essential.api.commands.*;
import gg.essential.universal.UChat;
import inforno.mcbmods.MCBMods;
import net.minecraft.client.network.NetworkPlayerInfo;

import javax.annotation.Nullable;
import java.util.Set;

import static inforno.mcbmods.MCBMods.mc;

public class ChatCommand extends Command {

    public static String chat = "";

    public ChatCommand() {
        super("fc");
    }

    @DefaultHandler
    public void handle() {
        UChat.chat(MCBMods.prefix + "§fAliases: /fc, /chat, /c, /factionchat. §bAlly Chat: /fc a, §aFaction Chat: /fc f, §fPublic Chat: /fc p, §fReply Chat: /fc r <player>");
    }

    @SubCommand(value = "a", description = "Ally Chat")
    public void allyChat() {
        chat = "#a";
        UChat.chat(MCBMods.prefix + "§bYou are now in ally chat.");
    }

    @SubCommand(value = "f", description = "Faction Chat")
    public void factionChat() {
        chat = "#f";
        UChat.chat(MCBMods.prefix + "§aYou are now in faction chat.");
    }

    @SubCommand(value = "p", description = "Public Chat")
    public void publicChat() {
        chat = "";
        UChat.chat(MCBMods.prefix + "§fYou are now in public chat.");
    }

    @SubCommand(value = "r", description = "Reply Chat")
    public void replayChat(@DisplayName("player") @Greedy String name) {
        for (NetworkPlayerInfo player : mc.getNetHandler().getPlayerInfoMap()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                chat = "/tell " + name;
                UChat.chat(MCBMods.prefix + "§fYou are now in reply chat.");
                return;
            }
        }
        UChat.chat(MCBMods.prefix + "§cPlease enter a valid username");
    }

    @Nullable
    @Override
    public Set<Alias> getCommandAliases() {
        return ImmutableSet.of(new Alias("chat"), new Alias("c"), new Alias("factionchat"));
    }
}
