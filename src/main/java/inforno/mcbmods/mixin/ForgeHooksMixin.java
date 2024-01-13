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

package inforno.mcbmods.mixin;

import inforno.mcbmods.config.MCBModsConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ISpecialArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

    /**
     * @author Inforno
     * @reason Vanilla-Flans Armor Value Display
     */
    @Overwrite(remap = false)
    public static int getTotalArmorValue(EntityPlayer player) {
        double ret = 0.0d;
        for (int x = 0; x < player.inventory.armorInventory.length; x++) {
            ItemStack stack = player.inventory.armorInventory[x];
            if (stack != null && stack.getItem() instanceof ISpecialArmor) {
                ret += ((ISpecialArmor) stack.getItem()).getArmorDisplay(player, stack, x);
            } else if (stack != null && stack.getItem() instanceof ItemArmor) {
                if (MCBModsConfig.fixArmorDisplay) {
                    ret += ((ItemArmor) stack.getItem()).damageReduceAmount / 25.0d * 20.0d;
                } else {
                    ret += ((ItemArmor) stack.getItem()).damageReduceAmount;
                }
            }
        }
        return (int) ret;
    }
}
