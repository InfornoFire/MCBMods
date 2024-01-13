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
import gg.essential.api.commands.SubCommand;
import gg.essential.universal.UChat;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;

import static inforno.mcbmods.MCBMods.mc;

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

        ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
        if (inventory == null) return;
        for (int i = smart ? 9 : 0; i < inventory.length; i++) {
            if (inventory[i] != null && (itemData = MCBMods.getWorth(inventory[i])) != null) {
                totalWorth += itemData[1] * inventory[i].stackSize;
            }
        }

        if (!smart) {
            ItemStack[] armorInventory = mc.thePlayer.inventory.armorInventory;
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
