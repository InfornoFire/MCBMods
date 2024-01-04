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
