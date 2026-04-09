package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 收藏传送点的持久化存储。
 *
 * 文件：.minecraft/config/warpgui-stars.json
 *
 * 格式：
 * {
 *   "warp": {
 *     "shengdian": ["spawn", "farm", "mine"],
 *     "sdmirror":  ["base"]
 *   },
 *   "home": {
 *     "shengdian": ["home1"]
 *   }
 * }
 *
 * key = serverId（与 WarpListManager.activeServerId 一致）
 * value = 收藏的传送点名称集合
 */
public class StarStorage {

    private static final Path STARS_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("warpgui-stars.json");

    private static volatile StarStorage INSTANCE;
    public static StarStorage get() {
        if (INSTANCE == null) INSTANCE = new StarStorage();
        return INSTANCE;
    }

    // 单线程后台执行器，序列化所有写盘操作，避免并发写入和频繁创建线程
    private final ExecutorService saveExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "warpgui-stars-save");
                t.setDaemon(true);
                return t;
            });

    // serverId -> Set<warpName>
    // 仅在主线程访问，无需并发容器
    private final Map<String, Set<String>> warpStars = new HashMap<>();
    private final Map<String, Set<String>> homeStars = new HashMap<>();

    private StarStorage() {
        load();
    }

    // ── 公开 API ──────────────────────────────────────────────────

    public Set<String> getStars(String serverId, boolean isHome) {
        serverId = norm(serverId);
        Map<String, Set<String>> map = isHome ? homeStars : warpStars;
        return map.computeIfAbsent(serverId, k -> new HashSet<>());
    }

    public boolean isStarred(String serverId, String name, boolean isHome) {
        return getStars(serverId, isHome).contains(name);
    }

    /** 切换收藏状态，立即持久化，返回新状态 */
    public boolean toggle(String serverId, String name, boolean isHome) {
        Set<String> set = getStars(serverId, isHome);
        boolean nowStarred;
        if (set.contains(name)) {
            set.remove(name);
            nowStarred = false;
        } else {
            set.add(name);
            nowStarred = true;
        }
        // 在主线程序列化 JSON，后台线程只做文件写入，避免后台线程访问共享 Map 造成数据竞争
        final String json = toJson();
        saveExecutor.execute(() -> writeFile(json));
        return nowStarred;
    }

    // ── 读写 ─────────────────────────────────────────────────────

    private void load() {
        if (!Files.exists(STARS_PATH)) return;
        try (Reader r = new InputStreamReader(
                Files.newInputStream(STARS_PATH), StandardCharsets.UTF_8)) {
            parse(readAll(r));
            int total = warpStars.values().stream().mapToInt(Set::size).sum()
                      + homeStars.values().stream().mapToInt(Set::size).sum();
            WarpGuiMod.LOGGER.info("[WarpGUI] 加载收藏: {} 个", total);
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 收藏加载失败: {}", e.getMessage());
        }
    }

    /** 仅由后台线程调用，接收已序列化的 JSON 字符串写入文件，不访问任何共享数据 */
    private void writeFile(String json) {
        try {
            Files.createDirectories(STARS_PATH.getParent());
            try (Writer w = new OutputStreamWriter(
                    Files.newOutputStream(STARS_PATH), StandardCharsets.UTF_8)) {
                w.write(json);
            }
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 收藏保存失败: {}", e.getMessage());
        }
    }

    // ── JSON ─────────────────────────────────────────────────────

    private String toJson() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"warp\": ");
        appendMap(sb, warpStars);
        sb.append(",\n  \"home\": ");
        appendMap(sb, homeStars);
        sb.append("\n}\n");
        return sb.toString();
    }

    private void appendMap(StringBuilder sb, Map<String, Set<String>> map) {
        sb.append("{\n");
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            Set<String> set = map.get(k);
            if (set.isEmpty()) continue;
            sb.append("    ").append(q(k)).append(": [");
            List<String> names = new ArrayList<>(set);
            Collections.sort(names);
            for (int j = 0; j < names.size(); j++) {
                sb.append(q(names.get(j)));
                if (j < names.size() - 1) sb.append(", ");
            }
            sb.append("]");
            if (i < keys.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }");
    }

    private void parse(String json) {
        String warpBlock = extractBlock(json, "\"warp\"");
        if (warpBlock != null) parseServerMap(warpBlock, warpStars);
        String homeBlock = extractBlock(json, "\"home\"");
        if (homeBlock != null) parseServerMap(homeBlock, homeStars);
    }

    private void parseServerMap(String block, Map<String, Set<String>> dest) {
        // 逐行找 "serverId": ["name1", "name2", ...]
        int pos = 0;
        while (pos < block.length()) {
            // 找 key
            int q1 = block.indexOf('"', pos); if (q1 < 0) break;
            int q2 = block.indexOf('"', q1 + 1); if (q2 < 0) break;
            String key = block.substring(q1 + 1, q2);
            pos = q2 + 1;
            // 找 [...]
            int ab = block.indexOf('[', pos); if (ab < 0) break;
            int cb = block.indexOf(']', ab);  if (cb < 0) break;
            String inner = block.substring(ab + 1, cb);
            pos = cb + 1;
            // 解析 names
            Set<String> set = new HashSet<>();
            int np = 0;
            while (np < inner.length()) {
                int nq1 = inner.indexOf('"', np); if (nq1 < 0) break;
                int nq2 = inner.indexOf('"', nq1 + 1); if (nq2 < 0) break;
                String name = inner.substring(nq1 + 1, nq2);
                if (!name.isEmpty()) set.add(name);
                np = nq2 + 1;
            }
            if (!key.isEmpty()) dest.put(key, new HashSet<>(set));
        }
    }

    // ── 工具 ─────────────────────────────────────────────────────

    private static String norm(String id) {
        return id == null ? "" : id.trim();
    }

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
