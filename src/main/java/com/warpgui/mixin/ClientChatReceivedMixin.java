package com.warpgui.mixin;

import com.warpgui.client.ServerDetector;
import com.warpgui.client.WarpListManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientChatReceivedMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), require = 0)
    private void warpgui$onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        try {
            Text message = packet.content();
            if (message == null) return;
            String raw = message.getString();
            // ServerDetector 仅用于记录当前服务器信息，不影响 WarpListManager 状态
            ServerDetector.tryUpdateFromChat(raw);
            // 传给 warp 列表解析器
            WarpListManager.getInstance().onChatMessage(raw);
        } catch (Exception ignored) {}
    }
}
