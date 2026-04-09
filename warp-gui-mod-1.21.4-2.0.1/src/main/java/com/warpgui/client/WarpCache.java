package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WarpCache {

    private static final Path CACHE_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("warpgui-cache.json");

    private static WarpCache INSTANCE;
    public static WarpCache get() {
        if (INSTANCE == null) INSTANCE = new WarpCache();
        return INSTANCE;
    }

    private final Map<String, List<WarpEntry>> warpCache = new HashMap<>();
    private final Map<String, List<WarpEntry>> homeCache = new HashMap<>();
    private final Map<String, Long>            lastRefreshTime = new HashMap<>();

    // 单线程后台写盘，主线程序列化好 JSON 再交给它
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "warpgui-cache-save");
        t.setDaemon(true); return t;
    });

    private WarpCache() { load(); }

    // ── 公开 API ──────────────────────────────────────────────────

    public List<WarpEntry> load(String serverId, boolean isHome) {
        serverId = norm(serverId);
        List<WarpEntry> cached = (isHome ? homeCache : warpCache)
                .getOrDefault(serverId, Collections.emptyList());
        List<WarpEntry> result = new ArrayList<>(cached.size());
        StarStorage ss = StarStorage.get();
        for (WarpEntry e : cached) {
            WarpEntry copy = new WarpEntry(e.name, e.comment, e.date, isHome);
            copy.starred = ss.isStarred(serverId, e.name, isHome);
            result.add(copy);
        }
        return result;
    }

    public void update(String serverId, boolean isHome, List<WarpEntry> entries) {
        serverId = norm(serverId);
        List<WarpEntry> snapshot = new ArrayList<>(entries.size());
        for (WarpEntry e : entries)
            snapshot.add(new WarpEntry(e.name, e.comment, e.date, isHome));
        (isHome ? homeCache : warpCache).put(serverId, snapshot);
        lastRefreshTime.put(serverId, System.currentTimeMillis());
        final String json = toJson();
        saveExecutor.execute(() -> writeFile(json));
        WarpGuiMod.LOGGER.info("[WarpGUI] 缓存更新: {} {} {} 条",
                serverId, isHome ? "home" : "warp", snapshot.size());
    }

    public int appendNew(String serverId, boolean isHome, List<WarpEntry> newEntries) {
        serverId = norm(serverId);
        List<WarpEntry> existing = (isHome ? homeCache : warpCache)
                .computeIfAbsent(serverId, k -> new ArrayList<>());
        Set<String> existingNames = new HashSet<>((int)(existing.size() / 0.75f) + 1);
        for (WarpEntry e : existing) existingNames.add(e.name);
        int added = 0;
        for (WarpEntry e : newEntries) {
            if (existingNames.add(e.name)) {  // add 返回 true → 新名称
                existing.add(new WarpEntry(e.name, e.comment, e.date, isHome));
                added++;
            }
        }
        lastRefreshTime.put(serverId, System.currentTimeMillis());
        final String json = toJson();
        saveExecutor.execute(() -> writeFile(json));
        if (added > 0)
            WarpGuiMod.LOGGER.info("[WarpGUI] 增量追加: {} {} +{} 条",
                    serverId, isHome ? "home" : "warp", added);
        else
            WarpGuiMod.LOGGER.info("[WarpGUI] 增量无新条目，时间戳已更新: {}", serverId);
        return added;
    }

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
            WarpGuiMod.LOGGER.info("[WarpGUI] 缓存加载: warp={} home={}", wc, hc);
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 缓存加载失败: {}", e.getMessage());
        }
    }

    private void writeFile(String json) {
        try {
            Files.createDirectories(CACHE_PATH.getParent());
            Files.writeString(CACHE_PATH, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 缓存写入失败: {}", e.getMessage());
        }
    }

    // ── JSON 序列化 ───────────────────────────────────────────────

    private String toJson() {
        int total = warpCache.values().stream().mapToInt(List::size).sum()
                  + homeCache.values().stream().mapToInt(List::size).sum();
        StringBuilder sb = new StringBuilder(total * 80 + 256);
        sb.append("{\n  \"warp\": ");   appendServerMap(sb, warpCache);
        sb.append(",\n  \"home\": ");   appendServerMap(sb, homeCache);
        sb.append(",\n  \"lastRefresh\": {\n");
        List<String> lrKeys = new ArrayList<>(lastRefreshTime.keySet());
        Collections.sort(lrKeys);
        for (int i = 0; i < lrKeys.size(); i++) {
            if (i > 0) sb.append(",\n");
            sb.append("    ").append(q(lrKeys.get(i)))
              .append(": ").append(lastRefreshTime.get(lrKeys.get(i)));
        }
        sb.append("\n  }\n}\n");
        return sb.toString();
    }

    private static void appendServerMap(StringBuilder sb, Map<String, List<WarpEntry>> map) {
        sb.append("{\n");
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        boolean first = true;
        for (String key : keys) {
            List<WarpEntry> entries = map.get(key);
            if (entries == null || entries.isEmpty()) continue;
            if (!first) sb.append(",\n"); first = false;
            sb.append("    ").append(q(key)).append(": [\n");
            for (int i = 0; i < entries.size(); i++) {
                WarpEntry e = entries.get(i);
                sb.append("      { \"name\": ").append(q(e.name))
                  .append(", \"comment\": ").append(q(e.comment))
                  .append(", \"date\": ").append(q(e.date)).append(" }");
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
        String lrBlock = extractBlock(json, "\"lastRefresh\"");
        if (lrBlock != null) parseLastRefresh(lrBlock);
    }

    private void parseLastRefresh(String block) {
        int pos = 0;
        while (pos < block.length()) {
            int q1 = block.indexOf('"', pos); if (q1 < 0) break;
            int q2 = nextQuote(block, q1 + 1); if (q2 < 0) break;
            String key = block.substring(q1 + 1, q2);
            pos = q2 + 1;
            int colon = block.indexOf(':', pos); if (colon < 0) break;
            pos = colon + 1;
            while (pos < block.length() && !Character.isDigit(block.charAt(pos))) pos++;
            int numEnd = pos;
            while (numEnd < block.length() && Character.isDigit(block.charAt(numEnd))) numEnd++;
            if (!key.isEmpty() && numEnd > pos) {
                try { lastRefreshTime.put(key, Long.parseLong(block, pos, numEnd, 10)); }
                catch (NumberFormatException ignored) {}
            }
            pos = numEnd;
        }
    }

    private void parseServerMap(String block, Map<String, List<WarpEntry>> dest, boolean isHome) {
        int pos = 0;
        while (pos < block.length()) {
            int q1 = block.indexOf('"', pos); if (q1 < 0) break;
            int q2 = nextQuote(block, q1 + 1); if (q2 < 0) break;
            String serverId = block.substring(q1 + 1, q2);
            pos = q2 + 1;
            int ab = block.indexOf('[', pos); if (ab < 0) break;
            int cb = findArrayEnd(block, ab); if (cb < 0) break;
            String arrStr = block.substring(ab + 1, cb);
            pos = cb + 1;
            if (!serverId.isEmpty()) dest.put(serverId, parseEntryArray(arrStr, isHome));
        }
    }

    private static List<WarpEntry> parseEntryArray(String arr, boolean isHome) {
        List<WarpEntry> list = new ArrayList<>();
        int pos = 0;
        while (pos < arr.length()) {
            int ob = arr.indexOf('{', pos); if (ob < 0) break;
            int cb = arr.indexOf('}', ob);  if (cb < 0) break;
            String obj = arr.substring(ob, cb + 1);
            pos = cb + 1;
            String name = extractStr(obj, "name");
            if (name != null && !name.isEmpty())
                list.add(new WarpEntry(name,
                        nvl(extractStr(obj, "comment")),
                        nvl(extractStr(obj, "date")), isHome));
        }
        return list;
    }

    // ── JSON 工具 ─────────────────────────────────────────────────

    private static String norm(String id) { return id == null ? "" : id.trim(); }
    private static String nvl(String s)   { return s != null ? s : ""; }

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
        int depth = 0; boolean inStr = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) { if (c == '"' && s.charAt(i-1) != '\\') inStr = false; }
            else if (c == '"') inStr = true;
            else if (c == '[') depth++;
            else if (c == ']') { if (--depth == 0) return i; }
        }
        return -1;
    }
    private static int nextQuote(String s, int from) {
        for (int i = from; i < s.length(); i++)
            if (s.charAt(i) == '"' && (i == 0 || s.charAt(i-1) != '\\')) return i;
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
