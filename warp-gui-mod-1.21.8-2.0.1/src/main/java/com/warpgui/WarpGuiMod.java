package com.warpgui;

import com.warpgui.client.ServerDetector;
import com.warpgui.config.WarpConfig;
import com.warpgui.client.WarpGuiScreen;
import com.warpgui.client.WarpListManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarpGuiMod implements ClientModInitializer {

    public static final String MOD_ID = "warpgui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[WarpGUI] 模组加载完成");
        WarpConfig.get(); // 初始化配置（首次运行生成默认 warpgui.json）

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "warpgui.keybind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "WarpGUI"
        ));

        // 连接时：尝试通过地址检测服务器（仅对单服有效，BungeeCord 子服无法通过地址区分）
        // WarpListManager 的活跃服务器由用户点击服务器标签决定，此处不设置
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ServerDetector.detectFromAddress(client));

        // 断线时重置 ServerDetector，WarpListManager 保留最后的状态供下次登录使用
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ServerDetector.reset());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new WarpGuiScreen());
                }
            }
        });
    }
}
