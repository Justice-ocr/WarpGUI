package com.warpgui.mixin;

import com.warpgui.client.ServerDetector;
import com.warpgui.client.WarpListManager;
import com.warpgui.config.WarpConfig;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截 ChatScreen.sendMessage()，在玩家提交聊天/命令时触发。
 * message 参数是玩家输入的原始字符串，命令以 "/" 开头。
 * 当检测到 /server {name} 时，清空缓存并从磁盘加载对应服务器的传送点列表。
 */
@Mixin(ChatScreen.class)
public class ClientSendCommandMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"), require = 0)
    private void warpgui$onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        try {
            if (message == null || message.isEmpty()) return;

            // 命令以 / 开头，去掉后得到实际命令字符串
            String cmd = message.startsWith("/") ? message.substring(1).trim() : null;
            if (cmd == null || cmd.isEmpty()) return;

            // 从配置中读取 switchServer 模板，如 "server {id}"
            String template = WarpConfig.get().commands.switchServer;
            if (template == null || !template.contains("{id}")) return;

            // 提取前缀，如 "server "
            String prefix = template.substring(0, template.indexOf("{id}"));
            if (!cmd.toLowerCase().startsWith(prefix.trim().toLowerCase())) return;

            // 提取 serverId
            String serverId = cmd.substring(prefix.trim().length()).trim();
            if (serverId.isEmpty()) return;

            // 在配置中查找匹配的服务器（大小写不敏感）
            WarpConfig.ServerEntry target = WarpConfig.get().servers.stream()
                    .filter(s -> s.id != null && s.id.equalsIgnoreCase(serverId))
                    .findFirst().orElse(null);

            if (target == null) return;

            // 更新 ServerDetector，清空内存缓存，从磁盘加载目标服务器列表
            ServerDetector.set(target);
            WarpListManager.getInstance().selectServer(target.id);

            com.warpgui.WarpGuiMod.LOGGER.info(
                    "[WarpGUI] 检测到 /server 命令，已加载: {}", target.displayName());

        } catch (Exception ignored) {}
    }
}
