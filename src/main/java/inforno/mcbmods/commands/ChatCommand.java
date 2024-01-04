package inforno.mcbmods.commands;

import com.google.common.collect.ImmutableSet;
import inforno.mcbmods.MCBMods;
import gg.essential.api.commands.*;
import gg.essential.universal.UChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import javax.annotation.Nullable;
import java.util.Set;

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
        for (NetworkPlayerInfo player : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                System.out.println(player.getGameProfile().getName());
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
