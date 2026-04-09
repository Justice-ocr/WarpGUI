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
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarpGuiMod implements ClientModInitializer {

    public static final String MOD_ID = "warpgui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding openGuiKey;

    // 1.21.9+：用 KeyBinding.Category.create() 静态工厂，而非 new KeyBinding.Category()
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of(MOD_ID, "category"));

    @Override
    public void onInitializeClient() {
        LOGGER.info("[WarpGUI] 模组加载完成");
        WarpConfig.get();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "warpgui.keybind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                ServerDetector.detectFromAddress(client));

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
