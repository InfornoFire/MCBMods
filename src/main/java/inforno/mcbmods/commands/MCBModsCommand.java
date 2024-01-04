package inforno.mcbmods.commands;

import inforno.mcbmods.MCBMods;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.api.utils.GuiUtil;

import java.util.Objects;

public class MCBModsCommand extends Command {

    public MCBModsCommand() {
        super("mcbmods");
    }

    @DefaultHandler
    public void handle() {
        GuiUtil.open(Objects.requireNonNull(MCBMods.config.gui()));
    }
}
