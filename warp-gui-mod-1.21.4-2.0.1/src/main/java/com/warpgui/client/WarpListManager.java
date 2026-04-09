package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import com.warpgui.config.WarpConfig;
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarpListManager {

    public enum Mode { WARP, HOME }
    private enum RefreshKind { FULL, INCREMENTAL }

    private static final WarpListManager INSTANCE = new WarpListManager();
    public static WarpListManager getInstance() { return INSTANCE; }

    // ── 正则预编译（编译一次，复用）────────────────────────────────
    private static final Pattern STRIP_COLOR =
            Pattern.compile("§[0-9a-fk-orA-FK-OR]");
    private static final Pattern WARP_HEADER =
            Pattern.compile("共享点列表\\s*[（(]第\\s*(\\d+)/(\\d+)\\s*页[）)]");
    private static final Pattern HOME_HEADER =
            Pattern.compile("传送点列表\\s*[（(]第\\s*(\\d+)/(\\d+)\\s*页[）)]");
    private static final Pattern LINE_PATTERN =
            Pattern.compile("^ID:(\\d+)\\s+(\\S+)(?:\\s+(.+?))?\\s+(\\d{4}-\\d{2}-\\d{2})\\s+\\d{2}:\\d{2}:\\d{2}\\s*$");
    private static final Pattern PAGE_LINE =
            Pattern.compile("[<《]上一页[>》]|[<《]下一页[>》]|第\\d+页/共\\d+页");

    // ── 存储：name→entry Map，O(1) 去重 + 保留插入顺序 ─────────────
    private final Map<String, Map<String, WarpEntry>> warpMaps = new HashMap<>();
    private final Map<String, Map<String, WarpEntry>> homeMaps = new HashMap<>();

    // 排序缓存（分 mode 独立失效，切换 mode 不重建另一侧）
    private List<WarpEntry> cachedSortedWarp = Collections.emptyList();
    private List<WarpEntry> cachedSortedHome = Collections.emptyList();
    private boolean dirtyWarp = true;
    private boolean dirtyHome = true;

    // 增量缓冲：name 作 key 天然去重
    private final Map<String, WarpEntry> incrementalBuf = new LinkedHashMap<>();

    // ── 抓取状态（全主线程访问，无需 volatile）─────────────────────
    private String      activeServerId = "";
    private Mode        fetchingMode   = Mode.WARP;
    private RefreshKind refreshKind    = RefreshKind.FULL;
    private int         currentPage    = 0;
    private int         totalPages     = 0;
    private boolean     capturing      = false;
    private boolean     fetchingAll    = false;
    private int         pendingPage    = 0;
    private int         ticksSinceSend = 0;

    private WarpListManager() {}

    private static String norm(String id) { return id == null ? "" : id.trim(); }
    private int fetchDelayTicks() { return WarpConfig.get().commands.pageDelayTicks; }

    // ── 服务器切换 ────────────────────────────────────────────────

    public void selectServer(String serverId) {
        serverId = norm(serverId);
        stopCapturing();
        activeServerId = serverId;
        dirtyWarp = dirtyHome = true;
        cachedSortedWarp = Collections.emptyList();
        cachedSortedHome = Collections.emptyList();

        // 从磁盘加载并建立 name→entry map
        warpMaps.put(serverId, toNameMap(WarpCache.get().load(serverId, false)));
        homeMaps.put(serverId, toNameMap(WarpCache.get().load(serverId, true)));

        WarpGuiMod.LOGGER.info("[WarpGUI] 选择服务器: {} warp={} home={}",
                serverId,
                warpMaps.get(serverId).size(),
                homeMaps.get(serverId).size());
    }

    // ── 刷新 ─────────────────────────────────────────────────────

    public void requestRefresh(MinecraftClient client, Mode mode) {
        if (client.player == null) return;
        fetchingMode = mode;
        refreshKind  = RefreshKind.FULL;
        incrementalBuf.clear();
        getOrCreateMap(mode == Mode.WARP ? warpMaps : homeMaps, activeServerId).clear();
        markDirty(mode);
        currentPage = totalPages = 0;
        capturing = fetchingAll = true;
        pendingPage = ticksSinceSend = 0;
        sendPage(client, 1, mode);
    }

    public void requestIncrementalRefresh(MinecraftClient client, Mode mode) {
        if (client.player == null) return;
        fetchingMode = mode;
        refreshKind  = RefreshKind.INCREMENTAL;
        incrementalBuf.clear();
        currentPage = totalPages = 0;
        capturing = true; fetchingAll = false;
        pendingPage = ticksSinceSend = 0;
        sendPage(client, 1, mode);
        WarpGuiMod.LOGGER.info("[WarpGUI] 开始增量刷新: {} {}", activeServerId, mode);
    }

    public void tick(MinecraftClient client) {
        if (pendingPage == 0 || !capturing) return;
        if (++ticksSinceSend >= fetchDelayTicks()) {
            int page = pendingPage;
            pendingPage = ticksSinceSend = 0;
            sendPage(client, page, fetchingMode);
        }
    }

    // ── 聊天解析 ──────────────────────────────────────────────────

    public void onChatMessage(String raw) {
        if (!capturing) return;
        String c = STRIP_COLOR.matcher(raw).replaceAll("").trim();
        if (c.isEmpty()) return;

        Matcher wh = WARP_HEADER.matcher(c);
        if (wh.find()) {
            fetchingMode = Mode.WARP;
            currentPage  = Integer.parseInt(wh.group(1));
            totalPages   = Integer.parseInt(wh.group(2));
            return;
        }
        Matcher hh = HOME_HEADER.matcher(c);
        if (hh.find()) {
            fetchingMode = Mode.HOME;
            currentPage  = Integer.parseInt(hh.group(1));
            totalPages   = Integer.parseInt(hh.group(2));
            return;
        }
        if (PAGE_LINE.matcher(c).find()) {
            if (refreshKind == RefreshKind.INCREMENTAL) handleIncrementalPageLine();
            else                                        handleFullPageLine();
            return;
        }
        Matcher m = LINE_PATTERN.matcher(c);
        if (!m.find()) return;

        String  name    = m.group(2).trim();
        String  comment = m.group(3) != null ? m.group(3).trim() : "";
        String  date    = m.group(4);
        boolean isHome  = fetchingMode == Mode.HOME;
        if (name.isEmpty()) return;

        if (refreshKind == RefreshKind.INCREMENTAL) {
            incrementalBuf.putIfAbsent(name, new WarpEntry(name, comment, date, isHome));
        } else {
            Map<String, WarpEntry> map =
                    getOrCreateMap(isHome ? homeMaps : warpMaps, activeServerId);
            if (map.putIfAbsent(name, null) == null) {           // key 不存在时插入
                WarpEntry e = new WarpEntry(name, comment, date, isHome);
                e.starred = StarStorage.get().isStarred(activeServerId, name, isHome);
                map.put(name, e);
                markDirty(fetchingMode);
            }
        }
    }

    private void handleFullPageLine() {
        if (fetchingAll && currentPage > 0 && currentPage < totalPages) {
            pendingPage = currentPage + 1; ticksSinceSend = 0;
        } else if (totalPages > 0 && currentPage >= totalPages) {
            fetchingAll = false; pendingPage = 0;
            flushToDisk(fetchingMode);
        }
    }

    private void handleIncrementalPageLine() {
        if (currentPage == 1 && totalPages > 1) {
            incrementalBuf.clear();
            pendingPage = totalPages; ticksSinceSend = 0;
        } else if (totalPages > 0 && currentPage >= totalPages) {
            capturing = false; pendingPage = 0;
            flushIncrementalToDisk(fetchingMode);
        }
    }

    private void flushToDisk(Mode mode) {
        boolean isHome = mode == Mode.HOME;
        Map<String, WarpEntry> map = getOrCreateMap(isHome ? homeMaps : warpMaps, activeServerId);
        List<WarpEntry> list = new ArrayList<>(map.values());
        WarpCache.get().update(activeServerId, isHome, list);
        markDirty(mode);
        WarpGuiMod.LOGGER.info("[WarpGUI] 写盘: {} {} {} 条",
                activeServerId, isHome ? "home" : "warp", list.size());
    }

    private void flushIncrementalToDisk(Mode mode) {
        boolean isHome = mode == Mode.HOME;
        List<WarpEntry> buf = new ArrayList<>(incrementalBuf.values());
        incrementalBuf.clear();
        int added = WarpCache.get().appendNew(activeServerId, isHome, buf);
        if (added > 0) {
            Map<String, WarpEntry> mem =
                    getOrCreateMap(isHome ? homeMaps : warpMaps, activeServerId);
            for (WarpEntry e : buf) {
                if (!mem.containsKey(e.name)) {
                    e.starred = StarStorage.get().isStarred(activeServerId, e.name, isHome);
                    mem.put(e.name, e);
                }
            }
            markDirty(mode);
        }
        WarpGuiMod.LOGGER.info("[WarpGUI] 增量完成: {} {} +{} 条",
                activeServerId, isHome ? "home" : "warp", added);
    }

    // ── 排序缓存（按需，分 mode 独立重建）──────────────────────────

    public List<WarpEntry> getList(Mode mode) {
        if (mode == Mode.WARP) {
            if (dirtyWarp) { cachedSortedWarp = buildSorted(warpMaps); dirtyWarp = false; }
            return cachedSortedWarp;
        } else {
            if (dirtyHome) { cachedSortedHome = buildSorted(homeMaps); dirtyHome = false; }
            return cachedSortedHome;
        }
    }

    private List<WarpEntry> buildSorted(Map<String, Map<String, WarpEntry>> maps) {
        Collection<WarpEntry> src = getOrCreateMap(maps, activeServerId).values();
        List<WarpEntry> list = new ArrayList<>(src);
        list.sort((a, b) -> {
            if (a.starred != b.starred) return a.starred ? -1 : 1;
            return a.name.compareToIgnoreCase(b.name);
        });
        return Collections.unmodifiableList(list);
    }

    private void markDirty(Mode mode) {
        if (mode == Mode.WARP) dirtyWarp = true;
        else                   dirtyHome = true;
    }

    // ── 公开 API ──────────────────────────────────────────────────

    public void toggleStar(WarpEntry e) {
        boolean now = StarStorage.get().toggle(activeServerId, e.name, e.isHome);
        e.starred = now;
        WarpEntry mem = getOrCreateMap(e.isHome ? homeMaps : warpMaps, activeServerId).get(e.name);
        if (mem != null) mem.starred = now;
        markDirty(e.isHome ? Mode.HOME : Mode.WARP);
    }

    public void stopCapturing() {
        capturing = fetchingAll = false;
        pendingPage = 0;
        incrementalBuf.clear();
    }

    public int    warpCount()         { return getOrCreateMap(warpMaps, activeServerId).size(); }
    public int    homeCount()         { return getOrCreateMap(homeMaps, activeServerId).size(); }
    public String getActiveServerId() { return activeServerId; }

    public boolean isLoading() {
        return capturing && (fetchingAll || pendingPage > 0 || totalPages == 0);
    }
    public boolean isIncrementalRefreshing() {
        return capturing && refreshKind == RefreshKind.INCREMENTAL;
    }

    // ── 工具 ─────────────────────────────────────────────────────

    private static Map<String, WarpEntry> toNameMap(List<WarpEntry> list) {
        Map<String, WarpEntry> m = new LinkedHashMap<>((int)(list.size() / 0.75f) + 1);
        for (WarpEntry e : list) m.put(e.name, e);
        return m;
    }

    private static Map<String, WarpEntry> getOrCreateMap(
            Map<String, Map<String, WarpEntry>> outer, String key) {
        return outer.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    private void sendPage(MinecraftClient client, int page, Mode mode) {
        if (client == null || client.player == null) return;
        try {
            client.player.networkHandler.sendChatCommand(
                    WarpConfig.get().buildListCmd(mode == Mode.HOME, page));
        } catch (Exception ex) {
            WarpGuiMod.LOGGER.warn("[WarpGUI] 发送命令失败: {}", ex.getMessage());
        }
    }
}
