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

import inforno.mcbmods.commands.ChatCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiScreen.class)
public class ChatMixin {

    @Shadow
    public Minecraft mc;

    @Redirect(
            method = "sendChatMessage(Ljava/lang/String;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;sendChatMessage(Ljava/lang/String;)V"
            )
    )
    public void sendChatMessage(EntityPlayerSP entityPlayerSP, String message) {
        if (!ChatCommand.chat.isEmpty()) {
            if (ChatCommand.chat.startsWith("/tell") && message.startsWith("/r ")) {
                message = message.substring(3);
            }
            if (!message.startsWith("/")) {
                this.mc.thePlayer.sendChatMessage(ChatCommand.chat + " " + message);
                return;
            }
        }
        this.mc.thePlayer.sendChatMessage(message);
    }
}
