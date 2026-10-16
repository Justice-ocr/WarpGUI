package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

/** Synchronizes MaLiLib's separate held-key cache without making it a required mod. */
public final class MalilibKeyCompat {
    private static boolean initialized;
    private static Method recheckPressedKeys;
    private static Method getActiveKeysString;

    private MalilibKeyCompat() {}

    public static void syncReleasedKeys() {
        if (!initialized) {
            initialized = true;
            if (!FabricLoader.getInstance().isModLoaded("malilib")) return;
            try {
                Class<?> keybindMulti = Class.forName("fi.dy.masa.malilib.hotkeys.KeybindMulti");
                Method recheck = keybindMulti.getMethod("reCheckPressedKeys");
                getActiveKeysString = keybindMulti.getMethod("getActiveKeysString");
                recheckPressedKeys = recheck;
                WarpGuiMod.LOGGER.info("[WarpGUI] MaLiLib held-key synchronization enabled");
            } catch (ReflectiveOperationException | LinkageError e) {
                WarpGuiMod.LOGGER.warn("[WarpGUI] MaLiLib held-key synchronization unavailable", e);
            }
        }
        if (recheckPressedKeys == null) return;

        try {
            String before = (String) getActiveKeysString.invoke(null);
            // Use MaLiLib's physical-state check: do not synthesize a release or clear held modifiers.
            recheckPressedKeys.invoke(null);
            String after = (String) getActiveKeysString.invoke(null);
            if (!before.equals(after)) {
                WarpGuiMod.LOGGER.info("[WarpGUI] MaLiLib released-key cache synchronized: [{}] -> [{}]",
                        before, after);
            }
        } catch (ReflectiveOperationException | LinkageError e) {
            recheckPressedKeys = null;
            WarpGuiMod.LOGGER.warn("[WarpGUI] MaLiLib held-key synchronization disabled after failure", e);
        }
    }
}
