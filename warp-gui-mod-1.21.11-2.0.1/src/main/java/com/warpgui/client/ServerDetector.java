package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import com.warpgui.config.WarpConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class ServerDetector {

    private static WarpConfig.ServerEntry current = null;

    private ServerDetector() {}

    public static void detectFromAddress(MinecraftClient client) {
        if (client.isIntegratedServerRunning()) { current = null; return; }
        ServerInfo info = client.getCurrentServerEntry();
        if (info == null) { current = null; return; }
        String combined = info.address.toLowerCase()
                + " "
                + (info.label != null ? info.label.getString().toLowerCase() : "");
        current = WarpConfig.get().detectServer(combined);
        WarpGuiMod.LOGGER.info("[WarpGUI] 地址检测: {}",
                current != null ? current.displayName() : "未知");
    }

    public static boolean tryUpdateFromChat(String raw) {
        WarpConfig.ServerEntry found = WarpConfig.get().detectFromChat(raw);
        if (found != null && found != current) {
            current = found;
            WarpGuiMod.LOGGER.info("[WarpGUI] 聊天检测切换: {}", found.displayName());
            return true;
        }
        return false;
    }

    public static WarpConfig.ServerEntry getCurrent() { return current; }

    public static void set(WarpConfig.ServerEntry e) {
        if (e != null) {
            current = WarpConfig.get().servers.stream()
                    .filter(s -> s.id != null && s.id.equals(e.id))
                    .findFirst().orElse(e);
        } else {
            current = null;
        }
        WarpGuiMod.LOGGER.info("[WarpGUI] 手动切换: {}",
                current != null ? current.displayName() : "null");
    }

    public static void reset() {
        current = null;
        WarpGuiMod.LOGGER.info("[WarpGUI] 检测器重置");
    }
}
