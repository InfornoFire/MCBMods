package inforno.mcbmods.commands;

import inforno.mcbmods.MCBMods;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.api.commands.SubCommand;
import gg.essential.universal.UChat;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;

public class InvWorthCommand extends Command {

    public InvWorthCommand() {
        super("invworth");
    }

    @DefaultHandler
    public void handle() {
        displayWorth(false);
    }

    @SubCommand(value = "smart", description = "Shows smart worth instead")
    public void publicChat() {
        displayWorth(true);
    }

    public void displayWorth(boolean smart) {
        float totalWorth = 0.0f;
        float[] itemData;

        ItemStack[] inventory = MCBMods.player.get().inventory.mainInventory;
        if (inventory == null) return;
        for (int i = smart ? 9 : 0; i < inventory.length; i++) {
            if (inventory[i] != null && (itemData = MCBMods.getWorth(inventory[i])) != null) {
                totalWorth += itemData[1] * inventory[i].stackSize;
            }
        }

        if (!smart) {
            ItemStack[] armorInventory = MCBMods.player.get().inventory.armorInventory;
            for (ItemStack itemStack : armorInventory) {
                if (itemStack != null && (itemData = MCBMods.getWorth(itemStack)) != null) {
                    totalWorth += itemData[1] * itemStack.stackSize;
                }
            }
        }

        DecimalFormat roundDisplay = new DecimalFormat("#.##");
        UChat.chat(MCBMods.prefix + "§6Your " + (smart ? "smart " : "") + "inventory is worth $" + roundDisplay.format(totalWorth) + " to Scrap Dealer!");
    }
}
