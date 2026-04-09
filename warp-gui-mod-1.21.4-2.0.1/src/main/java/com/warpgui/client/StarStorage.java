package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StarStorage {

    private static final Path STARS_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("warpgui-stars.json");

    private static StarStorage INSTANCE;
    public static StarStorage get() {
        if (INSTANCE == null) INSTANCE = new StarStorage();
        return INSTANCE;
    }

    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "warpgui-stars-save");
        t.setDaemon(true); return t;
    });

    private final Map<String, Set<String>> warpStars = new HashMap<>();
    private final Map<String, Set<String>> homeStars = new HashMap<>();

    private StarStorage() { load(); }

    // ── 公开 API ──────────────────────────────────────────────────

    public boolean isStarred(String serverId, String name, boolean isHome) {
        Set<String> set = (isHome ? homeStars : warpStars).get(norm(serverId));
        return set != null && set.contains(name);
    }

    /** 切换收藏：利用 Set.add/remove 返回值避免重复 contains 查询 */
    public boolean toggle(String serverId, String name, boolean isHome) {
        serverId = norm(serverId);
        Set<String> set = (isHome ? homeStars : warpStars)
                .computeIfAbsent(serverId, k -> new HashSet<>());
        boolean added = set.add(name);
        if (!added) set.remove(name);   // 已存在→移除
        final String json = toJson();
        saveExecutor.execute(() -> writeFile(json));
        return added;
    }

    // ── 读写 ─────────────────────────────────────────────────────

    private void load() {
        if (!Files.exists(STARS_PATH)) return;
        try (Reader r = new InputStreamReader(
                Files.newInputStream(STARS_PATH), StandardCharsets.UTF_8)) {
            parse(readAll(r));
            int total = warpStars.values().stream().mapToInt(Set::size).sum()
                      + homeStars.values().stream().mapToInt(Set::size).sum();
            WarpGuiMod.LOGGER.info("[WarpGUI] 收藏加载: {} 个", total);
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 收藏加载失败: {}", e.getMessage());
        }
    }

    private void writeFile(String json) {
        try {
            Files.createDirectories(STARS_PATH.getParent());
            Files.writeString(STARS_PATH, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 收藏写入失败: {}", e.getMessage());
        }
    }

    // ── JSON ─────────────────────────────────────────────────────

    private String toJson() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\n  \"warp\": "); appendMap(sb, warpStars);
        sb.append(",\n  \"home\": "); appendMap(sb, homeStars);
        sb.append("\n}\n");
        return sb.toString();
    }

    private static void appendMap(StringBuilder sb, Map<String, Set<String>> map) {
        sb.append("{\n");
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        boolean first = true;
        for (String k : keys) {
            Set<String> set = map.get(k);
            if (set == null || set.isEmpty()) continue;
            if (!first) sb.append(",\n"); first = false;
            sb.append("    ").append(q(k)).append(": [");
            List<String> names = new ArrayList<>(set);
            Collections.sort(names);
            for (int j = 0; j < names.size(); j++) {
                if (j > 0) sb.append(", ");
                sb.append(q(names.get(j)));
            }
            sb.append("]");
        }
        sb.append("\n  }");
    }

    private void parse(String json) {
        String w = extractBlock(json, "\"warp\"");
        if (w != null) parseServerMap(w, warpStars);
        String h = extractBlock(json, "\"home\"");
        if (h != null) parseServerMap(h, homeStars);
    }

    private static void parseServerMap(String block, Map<String, Set<String>> dest) {
        int pos = 0;
        while (pos < block.length()) {
            int q1 = block.indexOf('"', pos); if (q1 < 0) break;
            int q2 = block.indexOf('"', q1 + 1); if (q2 < 0) break;
            String key = block.substring(q1 + 1, q2);
            pos = q2 + 1;
            int ab = block.indexOf('[', pos); if (ab < 0) break;
            int cb = block.indexOf(']', ab);  if (cb < 0) break;
            String inner = block.substring(ab + 1, cb);
            pos = cb + 1;
            Set<String> set = new HashSet<>();
            int np = 0;
            while (np < inner.length()) {
                int nq1 = inner.indexOf('"', np); if (nq1 < 0) break;
                int nq2 = inner.indexOf('"', nq1 + 1); if (nq2 < 0) break;
                String name = inner.substring(nq1 + 1, nq2);
                if (!name.isEmpty()) set.add(name);
                np = nq2 + 1;
            }
            if (!key.isEmpty()) dest.put(key, set);
        }
    }

    private static String norm(String id) { return id == null ? "" : id.trim(); }
    private static String q(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
    private static String extractBlock(String json, String key) {
        int ki = json.indexOf(key); if (ki < 0) return null;
        int ob = json.indexOf('{', ki); if (ob < 0) return null;
        int depth = 0;
        for (int i = ob; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') { if (--depth == 0) return json.substring(ob + 1, i); }
        }
        return null;
    }
    private static String readAll(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096]; int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }
}
