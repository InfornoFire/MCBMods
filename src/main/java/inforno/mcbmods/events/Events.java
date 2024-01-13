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

package inforno.mcbmods.events;

import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.ShootableType;
import com.flansmod.common.teams.ItemTeamArmour;
import gg.essential.api.EssentialAPI;
import gg.essential.universal.UChat;
import gg.essential.universal.UDesktop;
import inforno.mcbmods.MCBMods;
import inforno.mcbmods.commands.AfkCommand;
import inforno.mcbmods.config.MCBModsConfig;
import inforno.mcbmods.notifications.Notifications;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.Display;

import java.net.URI;
import java.text.DecimalFormat;

import static inforno.mcbmods.MCBMods.mc;

public class Events {

    private final DecimalFormat df = new DecimalFormat("#.##");
    private final DecimalFormat dfp = new DecimalFormat("#.##%");

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        if (AfkCommand.afk && FMLEvents.afkchat) {
            if (unformatted.startsWith("> PM from >")) {
                UChat.say("/r " + MCBModsConfig.afkMessage);
                FMLEvents.afkchat = false;
            }
        }
        if (!Display.isActive()) {
            if (MCBModsConfig.notyifyMission && unformatted.startsWith("[MISSION] A random mission will start in one minute!")) {
                Notifications.notify("MCBMods", "A MCB Mission is about to start!");
            }
            if (MCBModsConfig.notifyName && unformatted.toLowerCase().contains(mc.thePlayer.getDisplayNameString().toLowerCase())) {
                Notifications.notify("MCBMods", unformatted);
            } else if (MCBModsConfig.notifyPM && unformatted.startsWith("> PM from >")) {
                Notifications.notify("MCBMods", unformatted);
            }
        }
    }

    @SubscribeEvent
    public void playerLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (MCBMods.versionChecker() > 0) {
            EssentialAPI.getNotifications().push("MCBMods", "New Version available! Click to update to " + MCBMods.latestVersion, () -> {
                UDesktop.browse(URI.create(MCBMods.latestVersionLink));
                return null;
            });
        }
        event.manager.channel().read().pipeline();
    }

    @SubscribeEvent
    public void playerLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        AfkCommand.afk = false;
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        final ItemStack stack = event.itemStack;
        if (stack == null) return;
        boolean newLine = false;

        if (MCBModsConfig.displayGunData) {
            if (stack.getItem() instanceof ItemGun) {
                GunType gun = ((ItemGun) stack.getItem()).type;
                if (!gun.shield) {
                    if (!GuiScreen.isShiftKeyDown()) {
                        event.toolTip.add("§8§o[Hold Shift for more information]");
                    } else if (gun.getMeleeDamage(stack) == 1.0f) {
                        // All ammo damage option
                        if (MCBModsConfig.displayDamage) {
                            if (MCBModsConfig.allAmmoDamage) {
                                event.toolTip.add("§cDamage -");
                                for (ShootableType ammo : gun.ammo) {
                                    event.toolTip.add(" §c" + ammo.name + ": " + df.format(gun.getDamage(stack) * gun.numBullets * ammo.damageVsLiving * ammo.numBullets / 2) + " §c❤");
                                }
                            } else {
                                float damageLiving = 1.0f;
                                if (!gun.ammo.isEmpty()) {
                                    damageLiving = gun.ammo.get(0).damageVsLiving * gun.ammo.get(0).numBullets;
                                }
                                event.toolTip.add("§cDamage: " + df.format(gun.getDamage(stack) * gun.numBullets * damageLiving / 2) + " §c❤");
                            }
                        }
                        if (MCBModsConfig.displaySpread) event.toolTip.add("§9Spread: " + gun.getSpread(stack));
                        if (MCBModsConfig.displayROF)
                            event.toolTip.add("§eRate of Fire: " + df.format((20 / gun.GetShootDelay(stack))) + " shots/sec");
                        GunType.GunRecoil recoil = gun.getRecoil(stack);
                        if (MCBModsConfig.displayRecoil) {
                            event.toolTip.add("§6Recoil -");
                            event.toolTip.add(" §6Vertical: " + df.format(recoil.vertical));
                            event.toolTip.add(" §6Horizontal: " + df.format(recoil.horizontal));
                        }
                        // if (gun.silenced || gun.getAttachment())
                        // event.toolTip.add("Silenced: " + gun.silenced);
                        if (MCBModsConfig.displaySwitchDelay)
                            event.toolTip.add("§aSwitch Delay: " + df.format(gun.switchDelay / 20) + " sec");
                        com.flansmod.common.guns.Paintjob paintjob = gun.getPaintjob(stack.getItem().getDamage(stack));
                        if (MCBModsConfig.displayPaintjob && paintjob.textureName != null)
                            event.toolTip.add("§dPaintjob: " + paintjob.textureName);
                        newLine = true;
                    } else if (gun.meleeTime > 1) {
                        if (MCBModsConfig.displayDamage)
                            event.toolTip.add("§cDamage: " + gun.getMeleeDamage(stack) / 2 + " §c❤");
                        if (MCBModsConfig.displayROF)
                            event.toolTip.add("§eRate of Attack: " + df.format((float) 20 / gun.meleeTime) + " attacks/sec");
                        newLine = true;
                    }
                }
            } else if (stack.getItem() instanceof ItemArmor) {
                if (!GuiScreen.isShiftKeyDown()) {
                    event.toolTip.add("§8§o[Hold Shift for more information]");
                } else {
                    ItemArmor armor = (ItemArmor) stack.getItem();
                    double defense;
                    if (armor instanceof ItemTeamArmour) {
                        defense = ((ItemTeamArmour) armor).type.defence * 0.7;
                    } else {
                        defense = armor.damageReduceAmount / 25.0;
                    }
                    event.toolTip.add("§9Bullet Damage Reduction: " + dfp.format(defense));
                    newLine = true;
                }
            }
        }

        if (MCBModsConfig.displayShopData) {
            float[] data = MCBMods.getWorth(stack);
            if (data != null) {
                int count = 1;
                String countStr = "";
                if (GuiScreen.isShiftKeyDown()) {
                    count = stack.stackSize;
                    countStr = "x" + count;
                }
                if (newLine) event.toolTip.add("");
                if (data[0] > 0) event.toolTip.add("§6Buy Price " + countStr + ": " + df.format(data[0] * count));
                event.toolTip.add("§6Sell Price " + countStr + ": " + df.format(data[1] * count));
                if (data[2] > 0) event.toolTip.add("§6Level: " + (int) data[2]);
            }
        }
    }

    @SubscribeEvent
    public void onGUIDrawnEvent(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (event != null && MCBModsConfig.chestWorth && (event.gui instanceof GuiChest || MCBMods.customEnderChest.isInstance(event.gui))) {
            GuiContainer chestGui = (GuiContainer) event.gui;
            IInventory chest = ((ContainerChest) chestGui.inventorySlots).getLowerChestInventory();
            float totalWorth = 0.0f;
            float[] worth;
            for (int i = 0; i < chest.getSizeInventory(); i++) {
                ItemStack stack = chest.getStackInSlot(i);
                if ((worth = MCBMods.getWorth(stack)) != null) totalWorth += worth[1] * stack.stackSize;
            }
            FontRenderer fr = mc.fontRendererObj;
            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(chestGui.guiLeft + (float) chestGui.xSize / 2, chestGui.guiTop, 1);
            fr.drawString("Worth: $" + df.format(totalWorth), -6, 6, 0x404040, false);
            GlStateManager.popMatrix();
        }
    }
}