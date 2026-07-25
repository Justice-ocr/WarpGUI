package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import com.warpgui.config.WarpConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

/**
 * 服务器检测器 —— 完全由 WarpConfig 驱动，不再硬编码关键词。
 */
public class ServerDetector {

    // 当前子服（对应 WarpConfig.ServerEntry，null 表示未知）
    private static WarpConfig.ServerEntry current = null;

    public static void detectFromAddress(MinecraftClient client) {
        if (client.isIntegratedServerRunning()) { current = null; return; }
        ServerInfo info = client.getCurrentServerEntry();
        if (info == null) { current = null; return; }
        String addr = info.address.toLowerCase();
        String motd = info.label != null ? info.label.getString().toLowerCase() : "";
        current = WarpConfig.get().detectServer(addr + " " + motd);
        WarpGuiMod.LOGGER.info("[WarpGUI] 地址检测服务器: {}",
                current != null ? current.displayName() : "未知");
    }

    public static boolean tryUpdateFromChat(String raw) {
        WarpConfig.ServerEntry found = WarpConfig.get().detectFromChat(raw);
        if (found != null && found != current) {
            current = found;
            WarpGuiMod.LOGGER.info("[WarpGUI] 聊天检测到切换: {}", found.displayName());
            return true;
        }
        return false;
    }

    public static WarpConfig.ServerEntry getCurrent() { return current; }

    public static void set(WarpConfig.ServerEntry e) {
        if (e != null) {
            // 从当前配置中找到同 id 的规范对象（防止旧引用失效）
            WarpConfig.ServerEntry canonical = WarpConfig.get().servers.stream()
                    .filter(s -> s.id != null && s.id.equals(e.id))
                    .findFirst().orElse(e);
            current = canonical;
        } else {
            current = null;
        }
        WarpGuiMod.LOGGER.info("[WarpGUI] 手动切换服务器: {}", current != null ? current.displayName() : "null");
    }

    public static void reset() {
        current = null;
        WarpGuiMod.LOGGER.info("[WarpGUI] 服务器检测已重置");
    }
}
