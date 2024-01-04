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
