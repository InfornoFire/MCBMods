package inforno.mcbmods.commands;

import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.api.utils.GuiUtil;
import inforno.mcbmods.gui.DamageCalcGui;

public class DamageCalcCommand extends Command {

    public DamageCalcCommand() {
        super("damagecalc");
    }

    @DefaultHandler
    public void handle() {
        GuiUtil.open(new DamageCalcGui());
    }
}