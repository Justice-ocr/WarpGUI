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

    private int fetchDelayTicks() { return WarpConfig.get().commands.pageDelayTicks; }

    private static final Pattern WARP_HEADER = Pattern.compile("共享点列表\\s*[（(]第\\s*(\\d+)/(\\d+)\\s*页[）)]");
    private static final Pattern HOME_HEADER = Pattern.compile("传送点列表\\s*[（(]第\\s*(\\d+)/(\\d+)\\s*页[）)]");
    private static final Pattern LINE_PATTERN =
        Pattern.compile("^ID:(\\d+)\\s+(\\S+)(?:\\s+(.+?))?\\s+(\\d{4}-\\d{2}-\\d{2})\\s+\\d{2}:\\d{2}:\\d{2}\\s*$");
    private static final Pattern PAGE_LINE = Pattern.compile("[<《]上一页[>》]|[<《]下一页[>》]|第\\d+页/共\\d+页");

    private final Map<String, List<WarpEntry>> warpLists = new HashMap<>();
    private final Map<String, List<WarpEntry>> homeLists = new HashMap<>();
    private List<WarpEntry> cachedSortedWarp = Collections.emptyList();
    private List<WarpEntry> cachedSortedHome = Collections.emptyList();
    private boolean sortDirty = true;

    private final List<WarpEntry> incrementalBuf = new ArrayList<>();

    private volatile String      activeServerId = "";
    private volatile Mode        fetchingMode   = Mode.WARP;
    private volatile RefreshKind refreshKind    = RefreshKind.FULL;
    private volatile int         currentPage    = 0;
    private volatile int         totalPages     = 0;
    private volatile boolean     capturing      = false;
    private volatile boolean     fetchingAll    = false;
    private volatile int         pendingPage    = 0;
    private volatile int         ticksSinceSend = 0;

    private WarpListManager() {}

    private static String norm(String id) { return id == null ? "" : id.trim(); }

    // ── 服务器选择 ────────────────────────────────────────────────

    public void selectServer(String serverId) {
        serverId = norm(serverId);
        stopCapturing();
        activeServerId = serverId;
        cachedSortedWarp = Collections.emptyList();
        cachedSortedHome = Collections.emptyList();
        sortDirty = true;

        warpLists.remove(serverId);
        homeLists.remove(serverId);

        List<WarpEntry> warps = WarpCache.get().load(serverId, false);
        List<WarpEntry> homes = WarpCache.get().load(serverId, true);
        getOrCreate(warpLists, serverId).addAll(warps);
        getOrCreate(homeLists, serverId).addAll(homes);

        sortDirty = true;
        WarpGuiMod.LOGGER.info("[WarpGUI] 选择服务器: {} warp={} home={}", serverId, warps.size(), homes.size());
    }

    // ── 刷新 ─────────────────────────────────────────────────────

    /** 完整刷新：从第 1 页拉到最后一页，覆盖内存和磁盘。 */
    public void requestRefresh(MinecraftClient client, Mode mode) {
        if (client.player == null) return;
        fetchingMode = mode;
        refreshKind  = RefreshKind.FULL;
        incrementalBuf.clear();
        getOrCreate(mode == Mode.WARP ? warpLists : homeLists, activeServerId).clear();
        sortDirty      = true;
        currentPage    = 0;
        totalPages     = 0;
        capturing      = true;
        fetchingAll    = true;
        pendingPage    = 0;
        ticksSinceSend = 0;
        sendPage(client, 1, mode);
    }

    /**
     * 增量刷新：先拉第 1 页获取总页数，若多页则跳转到最后一页，
     * 将识别到的且磁盘中不存在的条目追加进磁盘和内存。
     */
    public void requestIncrementalRefresh(MinecraftClient client, Mode mode) {
        if (client.player == null) return;
        fetchingMode = mode;
        refreshKind  = RefreshKind.INCREMENTAL;
        incrementalBuf.clear();
        currentPage    = 0;
        totalPages     = 0;
        capturing      = true;
        fetchingAll    = false;
        pendingPage    = 0;
        ticksSinceSend = 0;
        sendPage(client, 1, mode);
        WarpGuiMod.LOGGER.info("[WarpGUI] 开始增量刷新: {} {}", activeServerId, mode);
    }

    public void tick(MinecraftClient client) {
        if (pendingPage == 0 || !capturing) return;
        if (++ticksSinceSend >= fetchDelayTicks()) {
            int page = pendingPage;
            pendingPage = 0; ticksSinceSend = 0;
            sendPage(client, page, fetchingMode);
        }
    }

    public void onChatMessage(String raw) {
        if (!capturing) return;
        String c = raw.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
        if (c.isEmpty()) return;

        Matcher wh = WARP_HEADER.matcher(c);
        Matcher hh = HOME_HEADER.matcher(c);
        if (wh.find()) {
            fetchingMode = Mode.WARP;
            currentPage = Integer.parseInt(wh.group(1));
            totalPages  = Integer.parseInt(wh.group(2));
            return;
        }
        if (hh.find()) {
            fetchingMode = Mode.HOME;
            currentPage = Integer.parseInt(hh.group(1));
            totalPages  = Integer.parseInt(hh.group(2));
            return;
        }

        if (PAGE_LINE.matcher(c).find()) {
            if (refreshKind == RefreshKind.INCREMENTAL) handleIncrementalPageLine();
            else                                        handleFullPageLine();
            return;
        }

        Matcher m = LINE_PATTERN.matcher(c);
        if (m.find()) {
            String name    = m.group(2).trim();
            String comment = m.group(3) != null ? m.group(3).trim() : "";
            String date    = m.group(4);
            boolean isHome = fetchingMode == Mode.HOME;

            if (refreshKind == RefreshKind.INCREMENTAL) {
                if (!name.isEmpty() && findIn(incrementalBuf, name) == null)
                    incrementalBuf.add(new WarpEntry(name, comment, date, isHome));
            } else {
                List<WarpEntry> list = getOrCreate(isHome ? homeLists : warpLists, activeServerId);
                if (!name.isEmpty() && findIn(list, name) == null) {
                    WarpEntry e = new WarpEntry(name, comment, date, isHome);
                    e.starred = StarStorage.get().isStarred(activeServerId, name, isHome);
                    list.add(e);
                    sortDirty = true;
                }
            }
        }
    }

    private void handleFullPageLine() {
        if (fetchingAll && currentPage > 0 && currentPage < totalPages) {
            pendingPage = currentPage + 1;
            ticksSinceSend = 0;
        } else if (totalPages > 0 && currentPage >= totalPages) {
            fetchingAll = false;
            pendingPage = 0;
            flushToDisk(fetchingMode);
        }
    }

    private void handleIncrementalPageLine() {
        if (currentPage == 1 && totalPages > 1) {
            // 还有更多页：丢弃第 1 页收到的条目，跳转到最后一页
            incrementalBuf.clear();
            pendingPage = totalPages;
            ticksSinceSend = 0;
        } else if (totalPages > 0 && currentPage >= totalPages) {
            // 最后一页（或只有 1 页）完成
            capturing = false;
            pendingPage = 0;
            flushIncrementalToDisk(fetchingMode);
        }
    }

    private void flushToDisk(Mode mode) {
        boolean isHome = mode == Mode.HOME;
        List<WarpEntry> list = new ArrayList<>(getOrCreate(isHome ? homeLists : warpLists, activeServerId));
        WarpCache.get().update(activeServerId, isHome, list);
        sortDirty = true;
        WarpGuiMod.LOGGER.info("[WarpGUI] 写盘完成: {} {} {} 条", activeServerId, isHome ? "home" : "warp", list.size());
    }

    private void flushIncrementalToDisk(Mode mode) {
        boolean isHome = mode == Mode.HOME;
        List<WarpEntry> buf = new ArrayList<>(incrementalBuf);
        incrementalBuf.clear();
        int added = WarpCache.get().appendNew(activeServerId, isHome, buf);
        if (added > 0) {
            List<WarpEntry> memList = getOrCreate(isHome ? homeLists : warpLists, activeServerId);
            Set<String> existing = new HashSet<>();
            for (WarpEntry e : memList) existing.add(e.name);
            for (WarpEntry e : buf) {
                if (!existing.contains(e.name)) {
                    e.starred = StarStorage.get().isStarred(activeServerId, e.name, isHome);
                    memList.add(e);
                    existing.add(e.name);
                }
            }
            sortDirty = true;
        }
        WarpGuiMod.LOGGER.info("[WarpGUI] 增量刷新完成: {} {} 新增 {} 条", activeServerId, isHome ? "home" : "warp", added);
    }

    // ── 排序缓存 ──────────────────────────────────────────────────

    public List<WarpEntry> getList(Mode mode) {
        if (sortDirty) rebuildSortedCache();
        return mode == Mode.WARP ? cachedSortedWarp : cachedSortedHome;
    }

    private void rebuildSortedCache() {
        cachedSortedWarp = buildSorted(warpLists);
        cachedSortedHome = buildSorted(homeLists);
        sortDirty = false;
    }

    private List<WarpEntry> buildSorted(Map<String, List<WarpEntry>> map) {
        List<WarpEntry> src = new ArrayList<>(getOrCreate(map, activeServerId));
        src.sort((a, b) -> {
            if (a.starred != b.starred) return a.starred ? -1 : 1;
            return a.name.compareToIgnoreCase(b.name);
        });
        return Collections.unmodifiableList(src);
    }

    // ── 公开 API ──────────────────────────────────────────────────

    public void toggleStar(WarpEntry e) {
        boolean nowStarred = StarStorage.get().toggle(activeServerId, e.name, e.isHome);
        e.starred = nowStarred;
        for (WarpEntry entry : getOrCreate(e.isHome ? homeLists : warpLists, activeServerId))
            if (entry.name.equals(e.name)) entry.starred = nowStarred;
        sortDirty = true;
    }

    public void stopCapturing() {
        capturing = false; fetchingAll = false; pendingPage = 0;
        incrementalBuf.clear();
    }

    public int    warpCount()         { return getOrCreate(warpLists, activeServerId).size(); }
    public int    homeCount()         { return getOrCreate(homeLists, activeServerId).size(); }
    public String getActiveServerId() { return activeServerId; }

    public boolean isLoading() {
        // 完整刷新时 loading；增量刷新也短暂 loading（直到 capturing=false）
        return capturing && (fetchingAll || pendingPage > 0 || totalPages == 0);
    }

    /** 是否正在增量刷新（GUI 可据此显示轻量级"后台检查"提示） */
    public boolean isIncrementalRefreshing() {
        return capturing && refreshKind == RefreshKind.INCREMENTAL;
    }

    // ── 工具 ─────────────────────────────────────────────────────

    private List<WarpEntry> getOrCreate(Map<String, List<WarpEntry>> map, String key) {
        return map.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private WarpEntry findIn(List<WarpEntry> list, String name) {
        for (WarpEntry e : list) if (e.name.equals(name)) return e;
        return null;
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
