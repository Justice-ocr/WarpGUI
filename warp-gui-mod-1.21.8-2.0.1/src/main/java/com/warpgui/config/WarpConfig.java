package com.warpgui.config;

import com.warpgui.WarpGuiMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class WarpConfig {

    // ── 数据类 ────────────────────────────────────────────────────

    public static class Commands {
        public String warpList     = "warp list";
        public String homeList     = "home list";
        public String warpTp       = "warp tp {name}";
        public String homeTp       = "home tp {name}";
        public String switchServer = "server {id}";
        public int    pageDelayTicks = 10;
    }

    public static class ServerEntry {
        public String       name;
        public String       id;
        public List<String> keywords = new ArrayList<>();
        public boolean      hasWarp  = true;
        public boolean      hasHome  = false;
        public int          autoRefreshHours = 24;

        public ServerEntry() {}
        public ServerEntry(String name, String id, List<String> kw, boolean w, boolean h) {
            this.name = name; this.id = id;
            this.keywords = new ArrayList<>(kw);
            this.hasWarp = w; this.hasHome = h;
        }
        public String displayName() {
            return (name != null && !name.isEmpty()) ? name : id;
        }
    }

    // ── 单例 ─────────────────────────────────────────────────────

    private static WarpConfig INSTANCE;
    public static WarpConfig get() {
        if (INSTANCE == null) INSTANCE = new WarpConfig();
        return INSTANCE;
    }
    public static void reload() { INSTANCE = new WarpConfig(); }

    // ── 字段 ─────────────────────────────────────────────────────

    public Commands          commands = new Commands();
    public List<ServerEntry> servers  = new ArrayList<>();
    public String            uiTheme  = "light";  // "light" 或 "dark"

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("warpgui.json");

    // 聊天检测关键词预编译正则，避免每次 contains 多个字符串
    private static final Pattern CONN_PATTERN = Pattern.compile(
            "connect|transfer|connecting|switched|teleporting|传送到|已连接|已切换");
    // 去色预编译
    private static final Pattern STRIP_COLOR = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    private WarpConfig() { loadDefaults(); load(); }

    // ── 默认值 ───────────────────────────────────────────────────

    public void loadDefaults() {
        commands = new Commands();
        servers.clear();
        servers.add(new ServerEntry("生电服务器", "shengdian",
                Arrays.asList("shengdian"), true, true));
        servers.add(new ServerEntry("生电镜像", "sdmirror",
                Arrays.asList("sdmirror"), true, true));
        servers.add(new ServerEntry("提瓦特", "Teyvat",
                Arrays.asList("teyvat"), true, true));
        servers.add(new ServerEntry("登录服务器", "lobby",
                Arrays.asList("lobby"), false, false));
    }

    // ── 读写 ─────────────────────────────────────────────────────

    public void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try (Reader r = new InputStreamReader(
                Files.newInputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
            parseJson(readAll(r));
            WarpGuiMod.LOGGER.info("[WarpGUI] 配置加载: {} 个服务器", servers.size());
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 配置加载失败，使用默认值: {}", e.getMessage());
            loadDefaults();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, toJson(), StandardCharsets.UTF_8);
            WarpGuiMod.LOGGER.info("[WarpGUI] 配置已保存");
        } catch (Exception e) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 配置保存失败: {}", e.getMessage());
        }
    }

    public Path getConfigPath() { return CONFIG_PATH; }

    // ── 指令构建 ─────────────────────────────────────────────────

    public String buildListCmd(boolean isHome, int page) {
        String base = isHome ? commands.homeList : commands.warpList;
        return page <= 1 ? base : base + " " + page;
    }
    public String buildTpCmd(boolean isHome, String name) {
        return (isHome ? commands.homeTp : commands.warpTp).replace("{name}", name);
    }
    public String buildSwitchCmd(String serverId) {
        return commands.switchServer.replace("{id}", serverId);
    }

    // ── 服务器检测 ────────────────────────────────────────────────

    public ServerEntry detectServer(String combined) {
        String lower = combined.toLowerCase(Locale.ROOT);  // 一次转换，复用
        for (ServerEntry s : servers)
            for (String kw : s.keywords)
                if (!kw.isEmpty() && lower.contains(kw.toLowerCase(Locale.ROOT))) return s;
        return null;
    }

    public ServerEntry detectFromChat(String raw) {
        String clean = STRIP_COLOR.matcher(raw).replaceAll("").toLowerCase(Locale.ROOT);
        if (!CONN_PATTERN.matcher(clean).find()) return null;
        for (ServerEntry s : servers) {
            if (s.id != null && !s.id.isEmpty()
                    && clean.contains(s.id.toLowerCase(Locale.ROOT))) return s;
            for (String kw : s.keywords)
                if (!kw.isEmpty() && clean.contains(kw.toLowerCase(Locale.ROOT))) return s;
        }
        return null;
    }

    // ── JSON 序列化 ───────────────────────────────────────────────

    private String toJson() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\n  \"commands\": {\n");
        sb.append("    \"warpList\":       ").append(q(commands.warpList))    .append(",\n");
        sb.append("    \"homeList\":       ").append(q(commands.homeList))    .append(",\n");
        sb.append("    \"warpTp\":         ").append(q(commands.warpTp))      .append(",\n");
        sb.append("    \"homeTp\":         ").append(q(commands.homeTp))      .append(",\n");
        sb.append("    \"switchServer\":   ").append(q(commands.switchServer)).append(",\n");
        sb.append("    \"pageDelayTicks\": ").append(commands.pageDelayTicks) .append("\n");
        sb.append("  },\n  \"servers\": [\n");
        for (int i = 0; i < servers.size(); i++) {
            ServerEntry s = servers.get(i);
            sb.append("    {\n");
            sb.append("      \"name\":             ").append(q(s.name))           .append(",\n");
            sb.append("      \"id\":               ").append(q(s.id))             .append(",\n");
            sb.append("      \"keywords\":         [");
            for (int j = 0; j < s.keywords.size(); j++) {
                if (j > 0) sb.append(", ");
                sb.append(q(s.keywords.get(j)));
            }
            sb.append("],\n");
            sb.append("      \"hasWarp\":          ").append(s.hasWarp)           .append(",\n");
            sb.append("      \"hasHome\":          ").append(s.hasHome)           .append(",\n");
            sb.append("      \"autoRefreshHours\": ").append(s.autoRefreshHours)  .append("\n");
            sb.append("    }");
            if (i < servers.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"uiTheme\": ").append(q(uiTheme)).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private void parseJson(String json) {
        String cmdBlock = extractBlock(json, "\"commands\"");
        if (cmdBlock != null) {
            String v;
            if ((v = extractStr(cmdBlock, "warpList"))     != null) commands.warpList     = v;
            if ((v = extractStr(cmdBlock, "homeList"))     != null) commands.homeList     = v;
            if ((v = extractStr(cmdBlock, "warpTp"))       != null) commands.warpTp       = v;
            if ((v = extractStr(cmdBlock, "homeTp"))       != null) commands.homeTp       = v;
            if ((v = extractStr(cmdBlock, "switchServer")) != null) commands.switchServer = v;
            String dv = extractStr(cmdBlock, "pageDelayTicks");
            if (dv == null) dv = extractNum(cmdBlock, "pageDelayTicks");
            if (dv != null) {
                try { commands.pageDelayTicks =
                        Math.max(1, Math.min(100, Integer.parseInt(dv.trim()))); }
                catch (NumberFormatException ignored) {}
            }
        }
        String arrBlock = extractArrayBlock(json, "\"servers\"");
        if (arrBlock != null) {
            List<ServerEntry> parsed = parseServerArray(arrBlock);
            if (!parsed.isEmpty()) { servers.clear(); servers.addAll(parsed); }
        }
        String theme = extractStr(json, "uiTheme");
        if (theme != null && (theme.equals("light") || theme.equals("dark") || theme.equals("stylized"))) uiTheme = theme;
    }

    private List<ServerEntry> parseServerArray(String arr) {
        List<ServerEntry> list = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && --depth == 0 && start >= 0) {
                String obj = arr.substring(start, i + 1);
                ServerEntry e = new ServerEntry();
                String v;
                if ((v = extractStr(obj, "name")) != null) e.name = v;
                if ((v = extractStr(obj, "id"))   != null) e.id   = v;
                e.hasWarp  = extractBool(obj, "hasWarp",  true);
                e.hasHome  = extractBool(obj, "hasHome",  false);
                e.keywords = extractStrArray(obj, "keywords");
                String arh = extractNum(obj, "autoRefreshHours");
                if (arh != null) {
                    try { e.autoRefreshHours = Math.max(0, Integer.parseInt(arh.trim())); }
                    catch (NumberFormatException ignored) {}
                }
                if (e.id != null) list.add(e);
                start = -1;
            }
        }
        return list;
    }

    // ── JSON 工具 ─────────────────────────────────────────────────

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
            else if (c == '}') { if (--depth == 0) return json.substring(ob, i+1); }
        }
        return null;
    }
    private static String extractArrayBlock(String json, String key) {
        int ki = json.indexOf(key); if (ki < 0) return null;
        int ob = json.indexOf('[', ki); if (ob < 0) return null;
        int depth = 0;
        for (int i = ob; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { if (--depth == 0) return json.substring(ob+1, i); }
        }
        return null;
    }
    private static String extractStr(String obj, String key) {
        String pat = "\"" + key + "\"";
        int ki = obj.indexOf(pat); if (ki < 0) return null;
        int colon = obj.indexOf(':', ki + pat.length()); if (colon < 0) return null;
        int q1 = obj.indexOf('"', colon + 1); if (q1 < 0) return null;
        int q2 = q1 + 1;
        while (q2 < obj.length()) {
            if (obj.charAt(q2) == '"' && obj.charAt(q2-1) != '\\') break; q2++;
        }
        return obj.substring(q1+1, q2).replace("\\\"", "\"").replace("\\\\", "\\");
    }
    private static String extractNum(String obj, String key) {
        String pat = "\"" + key + "\"";
        int ki = obj.indexOf(pat); if (ki < 0) return null;
        int colon = obj.indexOf(':', ki + pat.length()); if (colon < 0) return null;
        int pos = colon + 1;
        while (pos < obj.length() && !Character.isDigit(obj.charAt(pos))) pos++;
        int end = pos;
        while (end < obj.length() && Character.isDigit(obj.charAt(end))) end++;
        return end > pos ? obj.substring(pos, end) : null;
    }
    private static boolean extractBool(String obj, String key, boolean def) {
        String pat = "\"" + key + "\"";
        int ki = obj.indexOf(pat); if (ki < 0) return def;
        int colon = obj.indexOf(':', ki + pat.length()); if (colon < 0) return def;
        String rest = obj.substring(colon + 1).trim();
        if (rest.startsWith("true"))  return true;
        if (rest.startsWith("false")) return false;
        return def;
    }
    private static List<String> extractStrArray(String obj, String key) {
        List<String> r = new ArrayList<>();
        String pat = "\"" + key + "\"";
        int ki = obj.indexOf(pat); if (ki < 0) return r;
        int ob = obj.indexOf('[', ki); if (ob < 0) return r;
        int cb = obj.indexOf(']', ob); if (cb < 0) return r;
        String inner = obj.substring(ob+1, cb);
        int pos = 0;
        while (pos < inner.length()) {
            int q1 = inner.indexOf('"', pos); if (q1 < 0) break;
            int q2 = q1 + 1;
            while (q2 < inner.length()) {
                if (inner.charAt(q2) == '"' && inner.charAt(q2-1) != '\\') break; q2++;
            }
            r.add(inner.substring(q1+1, q2)); pos = q2+1;
        }
        return r;
    }
    private static String readAll(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder(); char[] buf = new char[4096]; int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }
}
