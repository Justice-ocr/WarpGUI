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
 * 传送点列表的磁盘缓存。
 *
 * 文件：.minecraft/config/warpgui-cache.json
 *
 * 格式：
 * {
 *   "warp": {
 *     "shengdian": [
 *       { "name": "spawn", "comment": "出生点", "date": "2024-01-31" },
 *       ...
 *     ]
 *   },
 *   "home": {
 *     "shengdian": [
 *       { "name": "home1", "comment": "", "date": "2024-02-01" }
 *     ]
 *   }
 * }
 *
 * 每次完整刷新完一个服务器的列表后写盘；启动时自动读取，
 * 玩家打开 GUI 时无需等待刷新即可看到上次的列表。
 */
public class WarpCache {

    private static final Path CACHE_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("warpgui-cache.json");

    private static volatile WarpCache INSTANCE;
    public static WarpCache get() {
        if (INSTANCE == null) INSTANCE = new WarpCache();
        return INSTANCE;
    }

    // serverId -> List<WarpEntry>（只缓存 name/comment/date，starred 由 StarStorage 管）
    // 仅在主线程读取，后台线程只负责写盘（不访问这些 map）
    private final Map<String, List<WarpEntry>> warpCache = new HashMap<>();
    private final Map<String, List<WarpEntry>> homeCache = new HashMap<>();
    // serverId -> 上次完整刷新的 Unix 时间戳（毫秒）；两种 mode 共用同一时间戳
    private final Map<String, Long> lastRefreshTime = new HashMap<>();

    // 单线程后台执行器，序列化所有写盘操作
    private final ExecutorService saveExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "warpgui-cache-save");
                t.setDaemon(true);
                return t;
            });

    private WarpCache() {
        load();
    }

    // ── 公开 API ──────────────────────────────────────────────────

    /** 读取缓存，并用 StarStorage 恢复收藏状态，返回副本列表 */
    public List<WarpEntry> load(String serverId, boolean isHome) {
        serverId = norm(serverId);
        Map<String, List<WarpEntry>> map = isHome ? homeCache : warpCache;
        List<WarpEntry> cached = map.getOrDefault(serverId, Collections.emptyList());
        List<WarpEntry> result = new ArrayList<>(cached.size());
        for (WarpEntry e : cached) {
            WarpEntry copy = new WarpEntry(e.name, e.comment, e.date, isHome);
            copy.starred = StarStorage.get().isStarred(serverId, e.name, isHome);
            result.add(copy);
        }
        return result;
    }

    /**
     * 用刷新完毕的完整列表覆盖缓存，立即写盘。
     * 由 WarpListManager 在收到最后一页数据后调用。
     */
    public void update(String serverId, boolean isHome, List<WarpEntry> entries) {
        serverId = norm(serverId);
        // 深拷贝，避免持有 WarpListManager 内部列表的引用
        List<WarpEntry> snapshot = new ArrayList<>();
        for (WarpEntry e : entries) {
            snapshot.add(new WarpEntry(e.name, e.comment, e.date, isHome));
        }
        if (isHome) homeCache.put(serverId, snapshot);
        else        warpCache.put(serverId, snapshot);
        // 完整刷新完成时记录时间戳（warp 和 home 刷新时各更新一次，以最新为准）
        lastRefreshTime.put(serverId, System.currentTimeMillis());
        // 在主线程序列化 JSON，后台线程只做文件写入，避免后台线程访问共享 Map 造成数据竞争
        final String json = toJson();
        saveExecutor.execute(() -> writeFile(json));
        WarpGuiMod.LOGGER.info("[WarpGUI] 缓存已更新: {} {} {} 条",
                serverId, isHome ? "home" : "warp", snapshot.size());
    }

    /**
     * 增量追加：将 newEntries 中在磁盘缓存里不存在的条目写入缓存并保存。
     * 不覆盖现有条目。有新条目追加时同步更新 lastRefreshTime，
     * 避免每次打开 GUI 都重复触发自动刷新。
     * 返回实际追加的条目数。
     */
    public int appendNew(String serverId, boolean isHome, List<WarpEntry> newEntries) {
        serverId = norm(serverId);
        Map<String, List<WarpEntry>> map = isHome ? homeCache : warpCache;
        List<WarpEntry> existing = map.computeIfAbsent(serverId, k -> new ArrayList<>());
        // 建立现有名称集合，O(1) 查找
        Set<String> existingNames = new HashSet<>();
        for (WarpEntry e : existing) existingNames.add(e.name);
        int added = 0;
        for (WarpEntry e : newEntries) {
            if (!existingNames.contains(e.name)) {
                existing.add(new WarpEntry(e.name, e.comment, e.date, isHome));
                existingNames.add(e.name);
                added++;
            }
        }
        // 无论是否有新条目，都更新时间戳并写盘，避免下次打开 GUI 再次触发无意义的刷新
        lastRefreshTime.put(serverId, System.currentTimeMillis());
        final String json = toJson();
        saveExecutor.execute(() -> writeFile(json));
        if (added > 0) {
            WarpGuiMod.LOGGER.info("[WarpGUI] 增量追加: {} {} +{} 条", serverId, isHome?"home":"warp", added);
        } else {
            WarpGuiMod.LOGGER.info("[WarpGUI] 增量刷新无新条目，时间戳已更新: {}", serverId);
        }
        return added;
    }

    /** 获取指定服务器的上次刷新时间戳（毫秒），0 表示从未刷新 */
    public long getLastRefreshTime(String serverId) {
        Long t = lastRefreshTime.get(norm(serverId));
        return t != null ? t : 0L;
    }



    // ── 读写 ─────────────────────────────────────────────────────

    private void load() {
        if (!Files.exists(CACHE_PATH)) return;
        try (Reader r = new InputStreamReader(
                Files.newInputStream(CACHE_PATH), StandardCharsets.UTF_8)) {
            parse(readAll(r));
            int wc = warpCache.values().stream().mapToInt(List::size).sum();
            int hc = homeCache.values().stream().mapToInt(List::size).sum();
            WarpGuiMod.LOGGER.info("[WarpGUI] 缓存加载: warp {} 条, home {} 条", wc, hc);
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 缓存加载失败: {}", e.getMessage());
        }
    }

    /** 仅由后台线程调用，接收已序列化的 JSON 字符串写入文件，不访问任何共享数据 */
    private void writeFile(String json) {
        try {
            Files.createDirectories(CACHE_PATH.getParent());
            try (Writer w = new OutputStreamWriter(
                    Files.newOutputStream(CACHE_PATH), StandardCharsets.UTF_8)) {
                w.write(json);
            }
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 缓存保存失败: {}", e.getMessage());
        }
    }

    // ── JSON 序列化 ───────────────────────────────────────────────

    private String toJson() {
        StringBuilder sb = new StringBuilder("{\n");
        sb.append("  \"warp\": ");
        appendServerMap(sb, warpCache);
        sb.append(",\n  \"home\": ");
        appendServerMap(sb, homeCache);
        sb.append(",\n  \"lastRefresh\": {\n");
        List<String> lrKeys = new ArrayList<>(lastRefreshTime.keySet());
        Collections.sort(lrKeys);
        boolean firstLr = true;
        for (String k : lrKeys) {
            long ts = lastRefreshTime.get(k);
            if (!firstLr) sb.append(",\n");
            firstLr = false;
            sb.append("    ").append(q(k)).append(": ").append(ts);
        }
        sb.append("\n  }");
        sb.append("\n}\n");
        return sb.toString();
    }

    private void appendServerMap(StringBuilder sb, Map<String, List<WarpEntry>> map) {
        sb.append("{\n");
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        boolean firstKey = true;
        for (String key : keys) {
            List<WarpEntry> entries = map.get(key);
            if (entries == null || entries.isEmpty()) continue;
            if (!firstKey) sb.append(",\n");
            firstKey = false;
            sb.append("    ").append(q(key)).append(": [\n");
            for (int i = 0; i < entries.size(); i++) {
                WarpEntry e = entries.get(i);
                sb.append("      {")
                  .append(" \"name\": ").append(q(e.name)).append(",")
                  .append(" \"comment\": ").append(q(e.comment)).append(",")
                  .append(" \"date\": ").append(q(e.date))
                  .append(" }");
                if (i < entries.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("    ]");
        }
        sb.append("\n  }");
    }

    // ── JSON 解析 ─────────────────────────────────────────────────

    private void parse(String json) {
        String warpBlock = extractBlock(json, "\"warp\"");
        if (warpBlock != null) parseServerMap(warpBlock, warpCache, false);
        String homeBlock = extractBlock(json, "\"home\"");
        if (homeBlock != null) parseServerMap(homeBlock, homeCache, true);
        // 读取上次刷新时间戳
        String lrBlock = extractBlock(json, "\"lastRefresh\"");
        if (lrBlock != null) parseLastRefresh(lrBlock);
    }

    private void parseLastRefresh(String block) {
        // 格式: { "serverId": 1234567890, ... }  数字类型
        int pos = 0;
        while (pos < block.length()) {
            int q1 = block.indexOf('"', pos); if (q1 < 0) break;
            int q2 = nextQuote(block, q1 + 1); if (q2 < 0) break;
            String key = block.substring(q1 + 1, q2);
            pos = q2 + 1;
            int colon = block.indexOf(':', pos); if (colon < 0) break;
            pos = colon + 1;
            // 读数字
            StringBuilder num = new StringBuilder();
            while (pos < block.length()) {
                char c = block.charAt(pos);
                if (Character.isDigit(c)) { num.append(c); pos++; }
                else if (num.length() > 0) break;
                else pos++;
            }
            if (!key.isEmpty() && num.length() > 0) {
                try { lastRefreshTime.put(key, Long.parseLong(num.toString())); }
                catch (NumberFormatException ignored) {}
            }
        }
    }

    private void parseServerMap(String block, Map<String, List<WarpEntry>> dest, boolean isHome) {
        int pos = 0;
        while (pos < block.length()) {
            // 找 serverId key
            int q1 = block.indexOf('"', pos); if (q1 < 0) break;
            int q2 = nextQuote(block, q1 + 1); if (q2 < 0) break;
            String serverId = block.substring(q1 + 1, q2);
            pos = q2 + 1;

            // 找 [...]
            int ab = block.indexOf('[', pos); if (ab < 0) break;
            // 找对应的 ] （处理嵌套 {}）
            int cb = findArrayEnd(block, ab); if (cb < 0) break;
            String arrStr = block.substring(ab + 1, cb);
            pos = cb + 1;

            List<WarpEntry> entries = parseEntryArray(arrStr, isHome);
            if (!serverId.isEmpty()) dest.put(serverId, entries);
        }
    }

    private List<WarpEntry> parseEntryArray(String arr, boolean isHome) {
        List<WarpEntry> list = new ArrayList<>();
        int pos = 0;
        while (pos < arr.length()) {
            int ob = arr.indexOf('{', pos); if (ob < 0) break;
            int cb = arr.indexOf('}', ob);  if (cb < 0) break;
            String obj = arr.substring(ob, cb + 1);
            pos = cb + 1;

            String name    = extractStr(obj, "name");
            String comment = extractStr(obj, "comment");
            String date    = extractStr(obj, "date");
            if (name != null && !name.isEmpty()) {
                list.add(new WarpEntry(
                        name,
                        comment != null ? comment : "",
                        date    != null ? date    : "",
                        isHome));
            }
        }
        return list;
    }

    // ── 极简 JSON 工具 ────────────────────────────────────────────

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

    private static int findArrayEnd(String s, int start) {
        // start points at '[', find matching ']' skipping nested {} and ""
        int depth = 0;
        boolean inStr = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (c == '"' && s.charAt(i - 1) != '\\') inStr = false;
            } else {
                if (c == '"') inStr = true;
                else if (c == '[') depth++;
                else if (c == ']') { if (--depth == 0) return i; }
            }
        }
        return -1;
    }

    private static int nextQuote(String s, int from) {
        for (int i = from; i < s.length(); i++) {
            if (s.charAt(i) == '"' && (i == 0 || s.charAt(i - 1) != '\\')) return i;
        }
        return -1;
    }

    private static String extractStr(String obj, String key) {
        String pat = "\"" + key + "\"";
        int ki = obj.indexOf(pat); if (ki < 0) return null;
        int colon = obj.indexOf(':', ki + pat.length()); if (colon < 0) return null;
        int q1 = obj.indexOf('"', colon + 1); if (q1 < 0) return null;
        int q2 = nextQuote(obj, q1 + 1); if (q2 < 0) return null;
        return obj.substring(q1 + 1, q2).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String readAll(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192]; int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }
}
