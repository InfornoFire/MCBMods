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
