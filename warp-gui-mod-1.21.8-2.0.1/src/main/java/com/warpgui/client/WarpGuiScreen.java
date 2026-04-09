package com.warpgui.client;

import com.warpgui.WarpGuiMod;
import com.warpgui.config.ConfigScreen;
import com.warpgui.config.WarpConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * WarpGUI 主界面 — 三主题版（light / dark / stylized）
 */
public class WarpGuiScreen extends Screen {

    // ════════════════════════════════════════════════════════════
    // 色盘 — Light（明亮 / Frosted）
    // ════════════════════════════════════════════════════════════
    private static final int L_SCRIM       = 0x55000000;
    private static final int L_PANEL       = 0xFFF5F7FA;
    private static final int L_TOOLBAR     = 0xFFEBEEF3;
    private static final int L_SEARCH_BG   = 0xFFFFFFFF;
    private static final int L_BORDER      = 0xFFCDD3DC;
    private static final int L_DIVIDER     = 0x18000000;
    private static final int L_ACCENT      = 0xFF4A90D9;
    private static final int L_ACCENT_DK   = 0xFF3578C4;
    private static final int L_SRV_ACTIVE  = 0xFFDEECFB;
    private static final int L_SRV_HOVER   = 0x18000000;
    private static final int L_ROW_ODD     = 0x08000010;
    private static final int L_ROW_HOVER   = 0xFFEEF4FB;
    private static final int L_ROW_SEL     = 0xFFDCE9F7;
    private static final int L_TEXT        = 0xFF1A2330;
    private static final int L_TEXT_SEL    = 0xFF0A1828;
    private static final int L_MUTED       = 0xFF6B7A8D;
    private static final int L_DISABLED    = 0xFFB8C4D0;
    private static final int L_LOADING     = 0xFFE08820;
    private static final int L_STAR_ON     = 0xFFF5A623;
    private static final int L_STAR_OFF    = 0xFFCDD3DC;
    private static final int L_TP_BG       = 0xFF4A90D9;
    private static final int L_TP_HOVER    = 0xFF3578C4;

    // ════════════════════════════════════════════════════════════
    // 色盘 — Dark（深灰蓝）
    // ════════════════════════════════════════════════════════════
    private static final int D_SCRIM       = 0x88000000;
    private static final int D_PANEL       = 0xFF111820;
    private static final int D_TOOLBAR     = 0xFF182230;
    private static final int D_SEARCH_BG   = 0xFF0C1018;
    private static final int D_BORDER      = 0xFF2A3A50;
    private static final int D_DIVIDER     = 0x28AABBCC;
    private static final int D_ACCENT      = 0xFF3D6080;
    private static final int D_ACCENT_DK   = 0xFF2A4F6A;
    private static final int D_SRV_ACTIVE  = 0xFF1E3A54;
    private static final int D_SRV_HOVER   = 0x22AABBCC;
    private static final int D_ROW_ODD     = 0x10AABBCC;
    private static final int D_ROW_HOVER   = 0x22AABBCC;
    private static final int D_ROW_SEL     = 0x44558899;
    private static final int D_TEXT        = 0xFFCCDDEE;
    private static final int D_TEXT_SEL    = 0xFFFFFFFF;
    private static final int D_MUTED       = 0xFF6A8099;
    private static final int D_DISABLED    = 0xFF3A4A5A;
    private static final int D_LOADING     = 0xFFFFBB44;
    private static final int D_STAR_ON     = 0xFFFFCC44;
    private static final int D_STAR_OFF    = 0xFF3A4A5A;
    private static final int D_TP_BG       = 0xFF1E5080;
    private static final int D_TP_HOVER    = 0xFF2A6090;

    // ════════════════════════════════════════════════════════════
    // 色盘 — Stylized（原始深蓝紫风格）
    // ════════════════════════════════════════════════════════════
    private static final int S_OVERLAY     = 0x70000000;
    private static final int S_PANEL       = 0xF01A1A2E;
    private static final int S_HEADER      = 0xFF111830;
    private static final int S_SERVER_BAR  = 0xFF0C1020;
    private static final int S_SRV_ACTIVE  = 0xFF1A4070;
    private static final int S_SRV_IDLE    = 0xFF0D1E38;
    private static final int S_SRV_HOVER   = 0xFF153460;
    private static final int S_TAB_ON      = 0xFF0F3460;
    private static final int S_TAB_OFF     = 0x550A1E3A;
    private static final int S_LIST_BG     = 0xBB0A0A1A;
    private static final int S_ROW_EVEN    = 0x660D2B4D;
    private static final int S_ROW_ODD     = 0x44060F20;
    private static final int S_ROW_HOVER   = 0x990F3060;
    private static final int S_ROW_SEL     = 0xBBD04060;
    private static final int S_BORDER      = 0xFF4A2D80;
    private static final int S_COL_HEAD    = 0xFF1A2A50;
    private static final int S_NAME        = 0xFFDDEEFF;
    private static final int S_NAME_SEL    = 0xFFFFEE55;
    private static final int S_COMMENT     = 0xFF88AACC;
    private static final int S_DATE        = 0xFF607080;
    private static final int S_NUM         = 0xFF7080A0;
    private static final int S_DIM         = 0xFF8090A8;
    private static final int S_ACCENT      = 0xFFFF6B9D;
    private static final int S_LOADING     = 0xFFFFAA00;
    private static final int S_STAR_ON     = 0xFFFFCC00;
    private static final int S_STAR_OFF    = 0xFF50505A;
    private static final int S_BTN_TP      = 0xFF1A4A7A;
    private static final int S_BTN_STAR    = 0xFF2A2A3A;
    private static final int S_SEP         = 0x20AAAAFF;
    private static final int S_PAGE_BAR    = 0xFF0E1422;

    // ════════════════════════════════════════════════════════════
    // 运行时色盘指针（由 applyTheme() 更新）
    // ════════════════════════════════════════════════════════════
    // 通用颜色
    private int C_SCRIM, C_PANEL, C_BORDER, C_DIVIDER;
    private int C_ACCENT, C_LOADING;
    private int C_ROW_HOVER, C_ROW_SEL;
    private int C_TEXT, C_TEXT_SEL, C_MUTED, C_DISABLED;
    private int C_STAR_ON, C_STAR_OFF;
    // Light/Dark 专用
    private int C_TOOLBAR, C_SEARCH_BG, C_ACCENT_DK;
    private int C_SRV_ACTIVE, C_SRV_HOVER;
    private int C_ROW_ODD;
    private int C_TP_BG, C_TP_HOVER;
    // Stylized 专用（在 stylized 主题时才有意义）
    private int C_HEADER, C_SERVER_BAR;
    private int C_SRV_ACTIVE_S, C_SRV_IDLE, C_SRV_HOVER_S;
    private int C_TAB_ON, C_TAB_OFF, C_LIST_BG;
    private int C_ROW_EVEN, C_ROW_ODD_S;
    private int C_COL_HEAD, C_NAME, C_NAME_SEL, C_COMMENT, C_DATE, C_NUM, C_DIM;
    private int C_BTN_TP, C_BTN_STAR, C_SEP, C_PAGE_BAR;

    /** 当前主题标识 */
    private boolean isLight, isDark, isStylized;

    private void applyTheme() {
        String t = WarpConfig.get().uiTheme;
        isLight    = "light".equals(t);
        isDark     = "dark".equals(t);
        isStylized = "stylized".equals(t);
        if (!isLight && !isDark) isStylized = true; // 默认 fallback

        if (isLight) {
            C_SCRIM      = L_SCRIM;    C_PANEL    = L_PANEL;
            C_BORDER     = L_BORDER;   C_DIVIDER  = L_DIVIDER;
            C_ACCENT     = L_ACCENT;   C_LOADING  = L_LOADING;
            C_ROW_HOVER  = L_ROW_HOVER;C_ROW_SEL  = L_ROW_SEL;
            C_TEXT       = L_TEXT;     C_TEXT_SEL = L_TEXT_SEL;
            C_MUTED      = L_MUTED;    C_DISABLED = L_DISABLED;
            C_STAR_ON    = L_STAR_ON;  C_STAR_OFF = L_STAR_OFF;
            C_TOOLBAR    = L_TOOLBAR;  C_SEARCH_BG= L_SEARCH_BG;
            C_ACCENT_DK  = L_ACCENT_DK;
            C_SRV_ACTIVE = L_SRV_ACTIVE;C_SRV_HOVER= L_SRV_HOVER;
            C_ROW_ODD    = L_ROW_ODD;
            C_TP_BG      = L_TP_BG;   C_TP_HOVER = L_TP_HOVER;
        } else if (isDark) {
            C_SCRIM      = D_SCRIM;    C_PANEL    = D_PANEL;
            C_BORDER     = D_BORDER;   C_DIVIDER  = D_DIVIDER;
            C_ACCENT     = D_ACCENT;   C_LOADING  = D_LOADING;
            C_ROW_HOVER  = D_ROW_HOVER;C_ROW_SEL  = D_ROW_SEL;
            C_TEXT       = D_TEXT;     C_TEXT_SEL = D_TEXT_SEL;
            C_MUTED      = D_MUTED;    C_DISABLED = D_DISABLED;
            C_STAR_ON    = D_STAR_ON;  C_STAR_OFF = D_STAR_OFF;
            C_TOOLBAR    = D_TOOLBAR;  C_SEARCH_BG= D_SEARCH_BG;
            C_ACCENT_DK  = D_ACCENT_DK;
            C_SRV_ACTIVE = D_SRV_ACTIVE;C_SRV_HOVER= D_SRV_HOVER;
            C_ROW_ODD    = D_ROW_ODD;
            C_TP_BG      = D_TP_BG;   C_TP_HOVER = D_TP_HOVER;
        } else { // stylized
            C_SCRIM      = S_OVERLAY;  C_PANEL    = S_PANEL;
            C_BORDER     = S_BORDER;   C_DIVIDER  = S_SEP;
            C_ACCENT     = S_ACCENT;   C_LOADING  = S_LOADING;
            C_ROW_HOVER  = S_ROW_HOVER;C_ROW_SEL  = S_ROW_SEL;
            C_TEXT       = S_NAME;     C_TEXT_SEL = S_NAME_SEL;
            C_MUTED      = S_DIM;      C_DISABLED = S_DIM;
            C_STAR_ON    = S_STAR_ON;  C_STAR_OFF = S_STAR_OFF;
            // stylized 专用字段
            C_HEADER     = S_HEADER;   C_SERVER_BAR = S_SERVER_BAR;
            C_SRV_ACTIVE_S= S_SRV_ACTIVE;C_SRV_IDLE= S_SRV_IDLE;
            C_SRV_HOVER_S= S_SRV_HOVER;
            C_TAB_ON     = S_TAB_ON;   C_TAB_OFF  = S_TAB_OFF;
            C_LIST_BG    = S_LIST_BG;
            C_ROW_EVEN   = S_ROW_EVEN; C_ROW_ODD_S= S_ROW_ODD;
            C_COL_HEAD   = S_COL_HEAD;
            C_NAME       = S_NAME;     C_NAME_SEL = S_NAME_SEL;
            C_COMMENT    = S_COMMENT;  C_DATE     = S_DATE;
            C_NUM        = S_NUM;      C_DIM      = S_DIM;
            C_BTN_TP     = S_BTN_TP;  C_BTN_STAR = S_BTN_STAR;
            C_SEP        = S_SEP;      C_PAGE_BAR = S_PAGE_BAR;
        }
    }

    private int itemsPerPage = 10; // 动态计算，随屏幕高度变化
    private static final String[] SPIN = {"⠋","⠙","⠹","⠸","⠼","⠴","⠦","⠧","⠇","⠏"};

    // ── 布局（由 calcLayout 填入，风格化主题有独立字段）─────────
    private int pX, pY, pW, pH;
    private int toolbarH, serverBarH, tabH, searchH, colH, rowH, footerH;
    private int listX, listY, listW, listH, rowBtnY0;
    private int colNumW, colNameW, colCommentW, colDateW, colBtnW;

    // ── 状态 ──────────────────────────────────────────────────────
    private WarpListManager.Mode warpMode = WarpListManager.Mode.WARP;
    private boolean showBothModes = true;
    private TextFieldWidget searchField;
    private final List<WarpEntry> filtered = new ArrayList<>();
    private int cachedWarpCount, cachedHomeCount, guiPage, selIdx = -1;
    private int mouseX, mouseY, loadTick;
    private WarpConfig.ServerEntry cachedActiveSrv;

    private static final int MAX_ROWS = 20; // 按钮数组上限
    private ButtonWidget prevPageBtn, nextPageBtn, modeBtn;
    private ButtonWidget modernRefreshBtn, modernConfigBtn, modernCloseBtn;
    private final List<ButtonWidget> srvBtns  = new ArrayList<>();
    private final List<ButtonWidget> tabBtns  = new ArrayList<>();
    private final ButtonWidget[] rowTpBtns   = new ButtonWidget[MAX_ROWS];
    private final ButtonWidget[] rowStarBtns = new ButtonWidget[MAX_ROWS];
    private final ButtonWidget[] rowSelBtns  = new ButtonWidget[MAX_ROWS];

    public WarpGuiScreen() { super(Text.literal("WarpGUI")); }

    // ── 布局 ─────────────────────────────────────────────────────

    private void calcLayout() {
        if (isStylized) {
            pW = clamp((int)(width * 0.88), 440, 860);
            toolbarH = 28; serverBarH = 26; tabH = 24; searchH = 20;
            colH = 16; footerH = 22;
            int fixedH = toolbarH + serverBarH + tabH + 4 + searchH + 4 + colH + footerH + 20;
            // 目标行高 26~40，可用高度决定行数
            rowH = 28;
            int available = (int)(height * 0.94) - fixedH;
            itemsPerPage  = clamp(available / rowH, 4, MAX_ROWS);
            pH = fixedH + rowH * itemsPerPage;
            while (pH > height - 8 && itemsPerPage > 4) {
                itemsPerPage--;
                pH = fixedH + rowH * itemsPerPage;
            }
            pX = (width - pW) / 2; pY = (height - pH) / 2;
            listX = pX + 6;
            listY = pY + toolbarH + serverBarH + tabH + 4 + searchH + 4;
            listW = pW - 12; listH = colH + rowH * itemsPerPage; rowBtnY0 = listY + colH;
            colNumW = 26; colBtnW = 70;
            colDateW = textRenderer.getWidth("02-28") + 8;
            int rem = listW - colNumW - colDateW - colBtnW - 8;
            colNameW = Math.max(40, (int)(rem * 0.36)); colCommentW = rem - colNameW;
        } else {
            pW = clamp((int)(width * 0.86), 420, 820);
            toolbarH = 30; serverBarH = 0; tabH = 24; searchH = 20;
            colH = 16; footerH = 22;
            int fixedH = toolbarH + tabH + 4 + searchH + 4 + colH + footerH + 16;
            rowH = 28;
            int available = (int)(height * 0.92) - fixedH;
            itemsPerPage  = clamp(available / rowH, 4, MAX_ROWS);
            pH = fixedH + rowH * itemsPerPage;
            while (pH > height - 8 && itemsPerPage > 4) {
                itemsPerPage--;
                pH = fixedH + rowH * itemsPerPage;
            }
            pX = (width - pW) / 2; pY = (height - pH) / 2;
            listX = pX + 8;
            listY = pY + toolbarH + tabH + 4 + searchH + 4;
            listW = pW - 16; listH = colH + rowH * itemsPerPage; rowBtnY0 = listY + colH;
            colNumW = 0; colBtnW = 64;
            colDateW = textRenderer.getWidth("02-28") + 8;
            int rem  = listW - colDateW - colBtnW - 8;
            colNameW = Math.max(40, (int)(rem * 0.38)); colCommentW = rem - colNameW;
        }
    }

    private void refreshActiveSrv() {
        String id = WarpListManager.getInstance().getActiveServerId();
        cachedActiveSrv = WarpConfig.get().servers.stream()
                .filter(s -> s.id != null && s.id.trim().equals(id))
                .findFirst().orElse(null);
    }

    // ── 初始化 ────────────────────────────────────────────────────

    @Override
    protected void init() {
        applyTheme();
        calcLayout();
        refreshActiveSrv();

        // 搜索框
        int sfY = pY + toolbarH + serverBarH + tabH + 4;
        searchField = new TextFieldWidget(textRenderer,
                listX, sfY, listW, searchH, Text.literal("搜索"));
        searchField.setMaxLength(64);
        searchField.setPlaceholder(Text.literal("搜索名称或注释..."));
        searchField.setChangedListener(q -> { guiPage = 0; selIdx = -1; rebuildList(); });
        searchField.setDrawsBackground(false); // 禁用默认黑色背景，使用自定义圆角背景
        addSelectableChild(searchField);
        setInitialFocus(searchField);

        if (isStylized) {
            // 风格化：刷新+关闭在 headerH 区域
            int hbY = pY + (toolbarH - 18) / 2;
            mkBtn("↻ 刷新", pX + pW - 8 - 68, hbY, 68, this::doRefresh);
            mkBtn("✕",      pX + pW - 18,      pY + 4, 14, this::close);
            // ⚙ 在页码栏区域
            int pbY = listY + listH + (footerH - 18) / 2;
            prevPageBtn = mkBtn(" ◀ ", pX + 6,           pbY, 30, () -> changePage(-1));
            nextPageBtn = mkBtn(" ▶ ", pX + pW - 6 - 30, pbY, 30, () -> changePage(+1));
            mkBtn("⚙", pX + pW - 6 - 30 - 26, pbY, 22,
                    () -> client.setScreen(new ConfigScreen(this)));
        } else {
            // Light/Dark：工具栏右侧三个主题化图标按钮（透明 hotspot + 自定义渲染）
            // 布局：[标题] [服务器标签...] [warp+home] | [⚙][↻][✕]
            // 图标按钮区域固定在最右，各 22px 宽，4px 间距
            int iconH  = toolbarH - 10;
            int iconY  = pY + 5;
            int iconX3 = pX + pW - 8 - 22;           // ✕
            int iconX2 = iconX3 - 4 - 22;             // ↻
            int iconX1 = iconX2 - 4 - 22;             // ⚙
            modernCloseBtn  = mkHotspot(iconX3, iconY, 22, iconH, this::close);
            modernRefreshBtn= mkHotspot(iconX2, iconY, 22, iconH, this::doRefresh);
            modernConfigBtn = mkHotspot(iconX1, iconY, 22, iconH,
                    () -> client.setScreen(new ConfigScreen(this)));
            // 翻页按钮：页脚两端，同样用 hotspot
            int ftY = listY + listH + (footerH - 20) / 2;
            prevPageBtn = mkHotspot(pX + 8,       ftY, 28, 20, () -> changePage(-1));
            nextPageBtn = mkHotspot(pX + pW - 36, ftY, 28, 20, () -> changePage(+1));
        }

        buildServerButtons();
        buildTabButtons();
        buildRowButtons();

        if (cachedActiveSrv != null) {
            showBothModes = cachedActiveSrv.hasWarp && cachedActiveSrv.hasHome;
            if (!cachedActiveSrv.hasWarp && cachedActiveSrv.hasHome)
                warpMode = WarpListManager.Mode.HOME;
            if (!cachedActiveSrv.hasHome && cachedActiveSrv.hasWarp)
                warpMode = WarpListManager.Mode.WARP;
            maybeAutoRefresh(cachedActiveSrv);
        }
        guiPage = 0; selIdx = -1; rebuildList(); syncState();
    }

    private ButtonWidget mkBtn(String lbl, int x, int y, int w, Runnable r) {
        int h = isStylized ? 18 : 16;
        return addDrawableChild(ButtonWidget.builder(Text.literal(lbl), $ -> r.run())
                .dimensions(x, y, w, h).build());
    }
    private ButtonWidget mkHotspot(int x, int y, int w, int h, Runnable r) {
        ButtonWidget b = ButtonWidget.builder(Text.empty(), $ -> r.run())
                .dimensions(x, y, w, h).build();
        b.setAlpha(0f);
        return addDrawableChild(b);
    }

    private void buildServerButtons() {
        srvBtns.clear();
        List<WarpConfig.ServerEntry> all = WarpConfig.get().servers;
        int PAD = 10, GAP = 4;

        if (isStylized) {
            int barY = pY + toolbarH;
            int btnH = serverBarH - 6, btnY = barY + 3;
            int totalTagW = 0;
            for (WarpConfig.ServerEntry t : all)
                totalTagW += textRenderer.getWidth(t.displayName()) + PAD * 2 + GAP;
            if (!all.isEmpty()) totalTagW -= GAP;
            int modeBtnReserved = textRenderer.getWidth("▣ warp+home") + 22;
            int labelX = pX + 8 + textRenderer.getWidth("📍") + 4;
            int startX = Math.max(labelX,
                    (labelX + pX + pW - 8 - modeBtnReserved - totalTagW) / 2);
            int cx = startX;
            for (WarpConfig.ServerEntry srv : all) {
                int tw = textRenderer.getWidth(srv.displayName()) + PAD * 2;
                srvBtns.add(mkHotspot(cx, btnY, tw, btnH, () -> switchToServer(srv)));
                cx += tw + GAP;
            }
            int mBtnW = textRenderer.getWidth("▣ warp+home") + 12;
            modeBtn = mkHotspot(pX + pW - 8 - mBtnW,
                    barY + (serverBarH - (serverBarH - 6)) / 2, mBtnW, serverBarH - 6,
                    this::toggleMode);
        } else {
            // Light/Dark：服务器标签在 toolbar 内
            // 右侧固定保留：3个图标按钮(22*3) + 间距(4*2) + 右边距(8) = 82px
            // modeBtn 紧靠图标按钮左侧
            int tagH  = toolbarH - 10;
            int tagY  = pY + 5;
            int iconAreaW = 8 + 22 + 4 + 22 + 4 + 22; // 82px，右侧图标区
            int mBtnW = textRenderer.getWidth("warp+home") + 16;
            int rightReserved = iconAreaW + 6 + mBtnW; // 图标区 + 间距 + modeBtn
            int titleW = textRenderer.getWidth("传送点列表") + 18;
            int tagsStart = pX + 8 + titleW + 8;
            int tagsEnd   = pX + pW - rightReserved - 8;
            int totalTagW = 0;
            for (WarpConfig.ServerEntry t : all)
                totalTagW += textRenderer.getWidth(t.displayName()) + PAD * 2 + GAP;
            if (!all.isEmpty()) totalTagW -= GAP;
            int startX = tagsStart + Math.max(0, (tagsEnd - tagsStart - totalTagW) / 2);
            int cx = startX;
            for (WarpConfig.ServerEntry srv : all) {
                int tw = textRenderer.getWidth(srv.displayName()) + PAD * 2;
                srvBtns.add(mkHotspot(cx, tagY, tw, tagH, () -> switchToServer(srv)));
                cx += tw + GAP;
            }
            int modeBtnX = pX + pW - iconAreaW - 6 - mBtnW;
            modeBtn = mkHotspot(modeBtnX, tagY, mBtnW, tagH, this::toggleMode);
        }
    }

    private void buildTabButtons() {
        tabBtns.clear();
        if (!showBothModes) return;
        int tabY = pY + toolbarH + serverBarH;
        int tabW = (pW - 20) / 2;
        tabBtns.add(mkHotspot(pX + 8, tabY, tabW, tabH,
                () -> setWarpMode(WarpListManager.Mode.WARP)));
        tabBtns.add(mkHotspot(pX + 8 + tabW + 4, tabY, tabW - 4, tabH,
                () -> setWarpMode(WarpListManager.Mode.HOME)));
    }

    private void buildRowButtons() {
        for (int i = 0; i < itemsPerPage; i++) {
            final int fi = i;
            int iy  = rowBtnY0 + i * rowH;
            int tpX, tpW, stX, stW;
            if (isStylized) {
                tpX = listX + listW - colBtnW + 2; tpW = 34;
                stX = tpX + 38;                    stW = 24;
            } else {
                tpX = listX + listW - colBtnW;     tpW = 30;
                stX = tpX + 30 + 6;                stW = 20;
            }
            rowTpBtns[i]   = mkHotspot(tpX, iy + 2, tpW, rowH - 4,
                    () -> teleportTo(guiPage * itemsPerPage + fi));
            rowStarBtns[i] = mkHotspot(stX, iy + 2, stW, rowH - 4,
                    () -> onStarClick(fi));
            rowSelBtns[i]  = mkHotspot(listX, iy, tpX - listX, rowH,
                    () -> onRowClick(fi));
            boolean vis = (guiPage * itemsPerPage + i) < filtered.size();
            rowTpBtns[i].visible = rowStarBtns[i].visible = rowSelBtns[i].visible = vis;
        }
    }

    private void onStarClick(int row) {
        int ci = guiPage * itemsPerPage + row;
        if (ci >= 0 && ci < filtered.size()) {
            WarpListManager.getInstance().toggleStar(filtered.get(ci));
            rebuildList();
        }
    }
    private void onRowClick(int row) {
        int ci = guiPage * itemsPerPage + row;
        if (ci < filtered.size()) {
            if (selIdx == ci) teleportTo(ci);
            else { selIdx = ci; syncState(); }
        }
    }

    // ── 逻辑 ─────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        WarpListManager m = WarpListManager.getInstance();
        m.tick(client);
        if (m.isLoading() || m.isIncrementalRefreshing()) {
            loadTick++;
            if (loadTick % 3 == 0) rebuildList();
        }
    }

    private void rebuildList() {
        refreshActiveSrv();
        String q = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        filtered.clear();
        for (WarpEntry e : WarpListManager.getInstance().getList(warpMode))
            if (q.isEmpty() || e.name.toLowerCase().contains(q)
                            || e.comment.toLowerCase().contains(q))
                filtered.add(e);
        int mx = Math.max(0, (filtered.size() - 1) / itemsPerPage);
        if (guiPage > mx) guiPage = mx;
        cachedWarpCount = WarpListManager.getInstance().warpCount();
        cachedHomeCount = WarpListManager.getInstance().homeCount();
        updateRowVis(); syncState();
    }

    private void updateRowVis() {
        int ps = guiPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            boolean vis = (ps + i) < filtered.size();
            if (rowTpBtns[i]   != null) rowTpBtns[i].visible   = vis;
            if (rowStarBtns[i] != null) rowStarBtns[i].visible = vis;
            if (rowSelBtns[i]  != null) rowSelBtns[i].visible  = vis;
        }
    }

    private void syncState() {
        int mx = Math.max(0, (filtered.size() - 1) / itemsPerPage);
        if (prevPageBtn != null) prevPageBtn.active = guiPage > 0;
        if (nextPageBtn != null) nextPageBtn.active = guiPage < mx;
    }

    private void setWarpMode(WarpListManager.Mode m) {
        if (warpMode == m) return;
        warpMode = m; guiPage = 0; selIdx = -1; rebuildList();
        if ((m == WarpListManager.Mode.WARP ? cachedWarpCount : cachedHomeCount) == 0
                && client != null)
            WarpListManager.getInstance().requestRefresh(client, m);
    }

    private void toggleMode() {
        if (cachedActiveSrv == null
                || !cachedActiveSrv.hasWarp || !cachedActiveSrv.hasHome) return;
        showBothModes = !showBothModes; rebuildList();
    }

    private void maybeAutoRefresh(WarpConfig.ServerEntry srv) {
        if (srv == null || client == null || client.player == null
                || srv.autoRefreshHours <= 0) return;
        long last    = WarpCache.get().getLastRefreshTime(
                WarpListManager.getInstance().getActiveServerId());
        long elapsed = (System.currentTimeMillis() - last) / 3_600_000L;
        if (last == 0 || elapsed >= srv.autoRefreshHours) {
            WarpListManager.Mode m = srv.hasWarp
                    ? WarpListManager.Mode.WARP : WarpListManager.Mode.HOME;
            WarpListManager.getInstance().requestIncrementalRefresh(client, m);
            WarpGuiMod.LOGGER.info("[WarpGUI] 自动刷新: {} 距上次{}h 阈值{}h",
                    srv.displayName(), elapsed, srv.autoRefreshHours);
        }
    }

    private void doRefresh() {
        guiPage = 0; selIdx = -1;
        if (client != null) WarpListManager.getInstance().requestRefresh(client, warpMode);
        rebuildList();
    }

    private void switchToServer(WarpConfig.ServerEntry t) {
        if (client == null || client.player == null) return;
        try { client.player.networkHandler.sendChatCommand(
                WarpConfig.get().buildSwitchCmd(t.id)); }
        catch (Exception e) { WarpGuiMod.LOGGER.warn("[WarpGUI] 切换失败", e); }
        ServerDetector.set(t);
        WarpListManager.getInstance().selectServer(t.id);
        guiPage = 0; selIdx = -1;
        showBothModes = t.hasWarp && t.hasHome;
        if (!t.hasWarp && t.hasHome) warpMode = WarpListManager.Mode.HOME;
        if (!t.hasHome && t.hasWarp) warpMode = WarpListManager.Mode.WARP;
        refreshActiveSrv(); rebuildList(); syncState();
    }

    private void changePage(int d) {
        int mx = Math.max(0, (filtered.size() - 1) / itemsPerPage);
        guiPage = clamp(guiPage + d, 0, mx);
        selIdx = -1; updateRowVis(); syncState();
    }

    private void teleportTo(int idx) {
        if (idx < 0 || idx >= filtered.size()
                || client == null || client.player == null) return;
        WarpEntry e = filtered.get(idx);
        try {
            client.player.networkHandler.sendChatCommand(
                    WarpConfig.get().buildTpCmd(e.isHome, e.name));
            client.player.sendMessage(
                    Text.literal("§a[WarpGUI] §f→ §e" + e.name), true);
        } catch (Exception ex) { WarpGuiMod.LOGGER.warn("[WarpGUI] 传送失败", ex); }
        close();
    }

    // ── 渲染 分派 ─────────────────────────────────────────────────

    @Override public void renderBackground(DrawContext ctx, int mx, int my, float delta) {}

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        mouseX = mx; mouseY = my;
        if (isStylized) renderStylized(ctx, mx, my, delta);
        else            renderModern(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);
    }

    // ════════════════════════════════════════════════════════════
    // 渲染 — Light / Dark（现代简洁风）
    // ════════════════════════════════════════════════════════════

    private void renderModern(DrawContext ctx, int mx, int my, float delta) {
        boolean loading = WarpListManager.getInstance().isLoading();

        ctx.fill(0, 0, width, height, C_SCRIM);

        // 主面板 — r=8 圆角
        roundRect(ctx, pX, pY, pW, pH, C_BORDER, C_PANEL, 8);

        renderModernToolbar(ctx, loading);
        renderModernTabs(ctx);

        // 搜索框 — r=4 胶囊形
        int sfY = pY + toolbarH + tabH + 4;
        boolean focused = searchField != null && searchField.isFocused();
        int sfX = listX + 2, sfW = listW - 4;
        roundRect(ctx, sfX, sfY, sfW, searchH,
                focused ? C_ACCENT : C_BORDER, C_SEARCH_BG, 4);
        searchField.render(ctx, mx, my, delta);

        renderModernColHeader(ctx);
        renderModernListBody(ctx, loading);
        renderModernFooter(ctx);
    }

    private void renderModernToolbar(DrawContext ctx, boolean loading) {
        // Toolbar 顶部两角 r=8，底部平接 tab 区
        roundFillTop(ctx, pX, pY, pW, toolbarH, C_TOOLBAR, 8);
        // 左侧 accent 竖条（圆化 TL 角）
        roundFill(ctx, pX, pY, 3, toolbarH, C_ACCENT, 8, 0x1); // 仅 TL
        ctx.fill(pX, pY + toolbarH - 1, pX + pW, pY + toolbarH, C_BORDER);

        int ty = pY + (toolbarH - textRenderer.fontHeight) / 2;
        ctx.drawText(textRenderer, Text.literal("传送点列表"), pX + 10, ty, C_TEXT, false);

        WarpListManager wlm = WarpListManager.getInstance();
        if (wlm.isIncrementalRefreshing()) {
            String sp = SPIN[(loadTick / 3) % SPIN.length] + "  检查中";
            ctx.drawText(textRenderer, Text.literal(sp),
                    pX + pW / 2 - textRenderer.getWidth(sp) / 2, ty, C_MUTED, false);
        } else if (loading) {
            String sp = SPIN[(loadTick / 3) % SPIN.length] + "  加载中";
            ctx.drawText(textRenderer, Text.literal(sp),
                    pX + pW / 2 - textRenderer.getWidth(sp) / 2, ty, C_LOADING, false);
        }

        String activeId = WarpListManager.getInstance().getActiveServerId();
        List<WarpConfig.ServerEntry> all = WarpConfig.get().servers;
        for (int i = 0; i < srvBtns.size() && i < all.size(); i++) {
            ButtonWidget b  = srvBtns.get(i);
            WarpConfig.ServerEntry t = all.get(i);
            boolean active = t.id != null && t.id.trim().equals(activeId);
            boolean hover  = isHovering(b);
            if (active) {
                // 激活标签：pill 圆角背景 + 底部 accent 线
                roundFill(ctx, b.getX(), b.getY(), b.getWidth(), b.getHeight(), C_SRV_ACTIVE, 4);
                ctx.fill(b.getX() + 3, b.getY() + b.getHeight() - 2,
                        b.getX() + b.getWidth() - 3, b.getY() + b.getHeight(), C_ACCENT);
            } else if (hover) {
                roundFill(ctx, b.getX(), b.getY(), b.getWidth(), b.getHeight(), C_SRV_HOVER, 4);
            }
            int fg = active ? C_ACCENT : hover ? C_TEXT : C_MUTED;
            ctx.drawText(textRenderer, Text.literal(t.displayName()),
                    b.getX() + (b.getWidth() - textRenderer.getWidth(t.displayName())) / 2,
                    b.getY() + (b.getHeight() - textRenderer.fontHeight) / 2, fg, false);
        }

        if (modeBtn != null) {
            boolean canToggle = cachedActiveSrv != null
                    && cachedActiveSrv.hasWarp && cachedActiveSrv.hasHome;
            String lbl = !canToggle
                    ? (cachedActiveSrv != null && cachedActiveSrv.hasWarp ? "仅warp"
                    :  cachedActiveSrv != null && cachedActiveSrv.hasHome ? "仅home" : "无传送")
                    : showBothModes ? "warp+home" : "仅warp";
            boolean hover = isHovering(modeBtn);
            // 常态：圆角边框 + 轻底色；hover：加深底色
            int modeBg = !canToggle ? C_TOOLBAR
                    : hover ? C_SRV_ACTIVE
                    : (isDark ? 0xFF1A2A3A : 0xFFE2EBF4);
            roundRect(ctx, modeBtn.getX(), modeBtn.getY(),
                    modeBtn.getWidth(), modeBtn.getHeight(), C_BORDER, modeBg, 4);
            // 激活状态底部 accent 线
            if (canToggle && showBothModes)
                ctx.fill(modeBtn.getX() + 4, modeBtn.getY() + modeBtn.getHeight() - 2,
                        modeBtn.getX() + modeBtn.getWidth() - 4,
                        modeBtn.getY() + modeBtn.getHeight(), C_ACCENT);
            int fg = !canToggle ? C_DISABLED : hover ? C_ACCENT : C_TEXT;
            ctx.drawText(textRenderer, Text.literal(lbl),
                    modeBtn.getX() + (modeBtn.getWidth() - textRenderer.getWidth(lbl)) / 2,
                    modeBtn.getY() + (modeBtn.getHeight() - textRenderer.fontHeight) / 2,
                    fg, false);
            modeBtn.active = canToggle;
        }

        // ── 工具栏右侧图标按钮（⚙ ↻ ✕）自定义渲染 ─────────────────
        renderModernIconBtn(ctx, modernConfigBtn, "⚙", false);
        renderModernIconBtn(ctx, modernRefreshBtn, "↻", false);
        renderModernIconBtn(ctx, modernCloseBtn,   "✕", true);
    }

    /** 绘制工具栏图标按钮：常态有圆角边框+底色，hover 时加深，isDanger 时 hover 变红 */
    private void renderModernIconBtn(DrawContext ctx, ButtonWidget btn, String icon, boolean isDanger) {
        if (btn == null) return;
        boolean hover = isHovering(btn);
        int normalBg = isDark ? 0xFF182030 : 0xFFEBEEF3;
        int hoverBg  = isDanger
                ? (isDark ? 0xFF3A1515 : 0xFFFFE8E8)
                : C_SRV_ACTIVE;
        int bg = hover ? hoverBg : normalBg;
        roundRect(ctx, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), C_BORDER, bg, 4);
        int fg = isDanger
                ? (hover ? (isDark ? 0xFFFF7777 : 0xFFCC3333) : C_MUTED)
                : (hover ? C_ACCENT : C_TEXT);
        ctx.drawText(textRenderer, Text.literal(icon),
                btn.getX() + (btn.getWidth()  - textRenderer.getWidth(icon)) / 2,
                btn.getY() + (btn.getHeight() - textRenderer.fontHeight)      / 2,
                fg, false);
    }

    private void renderModernTabs(DrawContext ctx) {
        int tabY = pY + toolbarH;
        ctx.fill(pX, tabY, pX + pW, tabY + tabH, C_PANEL);
        ctx.fill(pX, tabY + tabH - 1, pX + pW, tabY + tabH, C_BORDER);

        if (!showBothModes) {
            String lbl = warpMode == WarpListManager.Mode.WARP
                    ? "共享传送点" + (cachedWarpCount > 0 ? "  " + cachedWarpCount : "")
                    : "个人传送点" + (cachedHomeCount > 0 ? "  " + cachedHomeCount : "");
            int tx = pX + 14;
            int ty = tabY + (tabH - textRenderer.fontHeight) / 2;
            ctx.drawText(textRenderer, Text.literal(lbl), tx, ty, C_ACCENT, false);
            ctx.fill(tx, tabY + tabH - 2,
                    tx + textRenderer.getWidth(lbl), tabY + tabH, C_ACCENT);
            return;
        }

        int tabW = (pW - 20) / 2;
        boolean wa = warpMode == WarpListManager.Mode.WARP;
        String wl = "共享传送点" + (cachedWarpCount > 0 ? "  " + cachedWarpCount : "");
        String hl = "个人传送点" + (cachedHomeCount > 0 ? "  " + cachedHomeCount : "");
        int ty = tabY + (tabH - textRenderer.fontHeight) / 2;
        boolean wDis = cachedActiveSrv != null && !cachedActiveSrv.hasWarp;
        boolean hDis = cachedActiveSrv != null && !cachedActiveSrv.hasHome;

        int wCx = pX + 8 + (tabW - textRenderer.getWidth(wl)) / 2;
        ctx.drawText(textRenderer, Text.literal(wl), wCx, ty,
                wDis ? C_DISABLED : wa ? C_ACCENT : C_MUTED, false);
        if (wa) ctx.fill(wCx, tabY + tabH - 2,
                wCx + textRenderer.getWidth(wl), tabY + tabH, C_ACCENT);

        int hCx = pX + 8 + tabW + 4 + ((tabW - 4) - textRenderer.getWidth(hl)) / 2;
        ctx.drawText(textRenderer, Text.literal(hl), hCx, ty,
                hDis ? C_DISABLED : !wa ? C_ACCENT : C_MUTED, false);
        if (!wa) ctx.fill(hCx, tabY + tabH - 2,
                hCx + textRenderer.getWidth(hl), tabY + tabH, C_ACCENT);

        ctx.fill(pX + 8 + tabW + 1, tabY + 5,
                pX + 8 + tabW + 2, tabY + tabH - 5, C_BORDER);
    }

    private void renderModernColHeader(DrawContext ctx) {
        ctx.fill(listX, listY, listX + listW, listY + colH, C_TOOLBAR);
        ctx.fill(listX, listY + colH - 1, listX + listW, listY + colH, C_BORDER);
        int fy = listY + (colH - textRenderer.fontHeight) / 2;
        int x  = listX + 8;
        ctx.drawText(textRenderer, Text.literal("名称"), x, fy, C_MUTED, false);
        x += colNameW;
        ctx.drawText(textRenderer, Text.literal("注释"), x, fy, C_MUTED, false);
        x += colCommentW;
        ctx.drawText(textRenderer, Text.literal("日期"), x, fy, C_MUTED, false);
    }

    private void renderModernListBody(DrawContext ctx, boolean loading) {
        ctx.fill(listX, listY + colH, listX + listW, listY + listH, C_PANEL);

        if (filtered.isEmpty()) {
            String msg = cachedActiveSrv != null
                    && !cachedActiveSrv.hasWarp && !cachedActiveSrv.hasHome
                    ? "该服务器暂不支持传送点功能"
                    : loading ? "正在加载..."
                    : cachedWarpCount == 0 && cachedHomeCount == 0 ? "暂无数据，按 ↻ 刷新"
                    : "无匹配结果";
            ctx.drawText(textRenderer, Text.literal(msg),
                    listX + (listW - textRenderer.getWidth(msg)) / 2,
                    listY + colH + (rowH * itemsPerPage - textRenderer.fontHeight) / 2,
                    loading ? C_LOADING : C_MUTED, false);
            return;
        }

        ctx.enableScissor(listX, listY + colH, listX + listW, listY + listH);
        int ps = guiPage * itemsPerPage;
        int fh = textRenderer.fontHeight;

        for (int i = 0; i < itemsPerPage; i++) {
            int idx = ps + i;
            if (idx >= filtered.size()) break;
            WarpEntry e  = filtered.get(idx);
            int iy       = rowBtnY0 + i * rowH;
            boolean sel  = idx == selIdx;
            boolean hov  = mouseX >= listX && mouseX < listX + listW
                        && mouseY >= iy && mouseY < iy + rowH;

            int rowBg = sel ? C_ROW_SEL : hov ? C_ROW_HOVER
                      : (i % 2 != 0 ? C_ROW_ODD : 0);
            if (rowBg != 0) ctx.fill(listX, iy, listX + listW, iy + rowH, rowBg);

            if (sel)           ctx.fill(listX, iy + 3, listX + 3, iy + rowH - 3, C_ACCENT);
            else if (e.starred) ctx.fill(listX, iy + 3, listX + 3, iy + rowH - 3, C_STAR_ON);

            if (i > 0) ctx.fill(listX + 8, iy, listX + listW - 8, iy + 1, C_DIVIDER);

            int fy = iy + (rowH - fh) / 2;
            int x  = listX + 8;

            int nameFg = sel ? C_TEXT_SEL : e.starred ? C_STAR_ON : C_TEXT;
            ctx.drawText(textRenderer,
                    Text.literal(truncate(e.name, colNameW - 8)), x, fy, nameFg, false);
            x += colNameW;
            if (e.hasComment())
                ctx.drawText(textRenderer,
                        Text.literal(truncate(e.comment, colCommentW - 8)),
                        x, fy, C_MUTED, false);
            x += colCommentW;
            if (!e.date.isEmpty())
                ctx.drawText(textRenderer,
                        Text.literal(e.shortDate()), x, fy, C_MUTED, false);

            // 传送按钮 — pill 圆角胶囊，始终可见（hover/sel 时填色，否则轮廓）
            int tpX = listX + listW - colBtnW;
            int stX = tpX + 30 + 6;
            boolean th = hov && mouseX >= tpX && mouseX < tpX + 30;
            boolean sh = hov && mouseX >= stX  && mouseX < stX  + 20;

            int tpColor = (sel || hov) ? (th ? C_TP_HOVER : C_TP_BG) : 0;
            if (tpColor != 0) {
                roundFill(ctx, tpX, iy + 4, 30, rowH - 8, tpColor, 4);
            } else {
                // 轮廓态：只画细边框胶囊
                roundFill(ctx, tpX, iy + 4, 30, rowH - 8, C_BORDER, 4);
                roundFill(ctx, tpX + 1, iy + 5, 28, rowH - 10, C_PANEL, 4);
            }
            int tpFg = (sel || hov) ? C_TEXT_SEL : C_MUTED;
            Text tpLabel = Text.literal("传送");
            ctx.drawText(textRenderer, tpLabel,
                    tpX + (30 - textRenderer.getWidth(tpLabel)) / 2, fy, tpFg, false);

            // 星标（圆形背景）
            String star = e.starred ? "★" : "☆";
            if (e.starred || sh) {
                roundFill(ctx, stX, iy + 4, 20, rowH - 8,
                        e.starred ? (isDark ? 0x33FFCC44 : 0x22F5A623) : C_SRV_HOVER, 4);
            }
            ctx.drawText(textRenderer, Text.literal(star),
                    stX + (20 - textRenderer.getWidth(star)) / 2, fy,
                    e.starred ? C_STAR_ON : sh ? C_STAR_ON : C_STAR_OFF, false);
        }
        ctx.disableScissor();
    }

    private void renderModernFooter(DrawContext ctx) {
        int barY = listY + listH;
        // 底部两角 r=8，顶部平接列表
        roundFillBottom(ctx, pX, barY, pW, footerH, C_TOOLBAR, 8);
        ctx.fill(pX, barY, pX + pW, barY + 1, C_BORDER);

        // 翻页按钮（◀ / ▶）主题化渲染
        int mx2 = Math.max(0, (filtered.size() - 1) / itemsPerPage);
        renderModernPageBtn(ctx, prevPageBtn, "◀", guiPage > 0);
        renderModernPageBtn(ctx, nextPageBtn, "▶", guiPage < mx2);

        // 页码信息居中
        String info = (guiPage + 1) + " / " + (mx2 + 1) + "  ·  " + filtered.size() + " 个传送点";
        ctx.drawText(textRenderer, Text.literal(info),
                pX + (pW - textRenderer.getWidth(info)) / 2,
                barY + (footerH - textRenderer.fontHeight) / 2,
                C_MUTED, false);
    }

    /** 绘制翻页按钮：可用时有边框，hover 时填色 */
    private void renderModernPageBtn(DrawContext ctx, ButtonWidget btn, String icon, boolean active) {
        if (btn == null) return;
        boolean hover = active && isHovering(btn);
        int fg, bgColor;
        if (!active) {
            fg = C_DISABLED; bgColor = 0;
        } else if (hover) {
            fg = C_TEXT_SEL; bgColor = C_TP_BG;
        } else {
            fg = C_MUTED; bgColor = 0;
        }
        if (bgColor != 0)
            roundFill(ctx, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), bgColor, 4);
        else if (active)
            roundRect(ctx, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), C_BORDER, C_TOOLBAR, 3);
        ctx.drawText(textRenderer, Text.literal(icon),
                btn.getX() + (btn.getWidth()  - textRenderer.getWidth(icon)) / 2,
                btn.getY() + (btn.getHeight() - textRenderer.fontHeight)      / 2,
                fg, false);
    }

    // ════════════════════════════════════════════════════════════
    // 渲染 — Stylized（原始深蓝紫风格，完整还原）
    // ════════════════════════════════════════════════════════════

    private void renderStylized(DrawContext ctx, int mx, int my, float delta) {
        boolean loading = WarpListManager.getInstance().isLoading();

        ctx.fill(0, 0, width, height, C_SCRIM);
        ctx.fill(pX, pY, pX + pW, pY + pH, C_PANEL);
        border(ctx, pX, pY, pW, pH, C_BORDER);

        renderStylizedHeader(ctx, loading);
        renderStylizedServerBar(ctx);
        renderStylizedTabs(ctx);

        searchField.render(ctx, mx, my, delta);

        renderStylizedColHeader(ctx);
        ctx.fill(listX, listY + colH, listX + listW, listY + listH, C_LIST_BG);

        if (filtered.isEmpty()) {
            String msg = cachedActiveSrv != null
                    && !cachedActiveSrv.hasWarp && !cachedActiveSrv.hasHome
                    ? "该服务器暂不支持传送点功能"
                    : loading ? "正在加载..."
                    : cachedWarpCount == 0 && cachedHomeCount == 0
                    ? "暂无数据，请点击 ↻ 刷新" : "没有匹配结果";
            ctx.drawText(textRenderer, Text.literal(msg),
                    listX + (listW - textRenderer.getWidth(msg)) / 2,
                    listY + colH + (rowH * itemsPerPage - textRenderer.fontHeight) / 2,
                    loading ? C_LOADING : C_DIM, true);
        } else {
            renderStylizedRows(ctx);
        }

        renderStylizedPageBar(ctx);
    }

    private void renderStylizedHeader(DrawContext ctx, boolean loading) {
        ctx.fill(pX, pY, pX + pW, pY + toolbarH, C_HEADER);
        border(ctx, pX, pY, pW, toolbarH, C_BORDER);
        int ty = pY + (toolbarH - textRenderer.fontHeight) / 2;
        ctx.drawText(textRenderer, Text.literal("✦ 传送点列表 ✦"), pX + 10, ty, C_ACCENT, true);
        WarpListManager wlm = WarpListManager.getInstance();
        if (wlm.isIncrementalRefreshing()) {
            String sp = SPIN[(loadTick / 3) % SPIN.length] + " 后台检查中";
            ctx.drawText(textRenderer, Text.literal(sp),
                    pX + pW / 2 - textRenderer.getWidth(sp) / 2, ty, 0xFF80A0C0, true);
        } else if (loading) {
            String sp = SPIN[(loadTick / 3) % SPIN.length] + " 加载中...";
            ctx.drawText(textRenderer, Text.literal(sp),
                    pX + pW / 2 - textRenderer.getWidth(sp) / 2, ty, C_LOADING, true);
        }
    }

    private void renderStylizedServerBar(DrawContext ctx) {
        int barY = pY + toolbarH;
        ctx.fill(pX, barY, pX + pW, barY + serverBarH, C_SERVER_BAR);
        border(ctx, pX, barY, pW, serverBarH, C_BORDER);
        ctx.drawText(textRenderer, Text.literal("📍"),
                pX + 8, barY + (serverBarH - textRenderer.fontHeight) / 2, C_DIM, true);

        String activeId = WarpListManager.getInstance().getActiveServerId();
        List<WarpConfig.ServerEntry> all = WarpConfig.get().servers;
        for (int i = 0; i < srvBtns.size() && i < all.size(); i++) {
            ButtonWidget b = srvBtns.get(i);
            WarpConfig.ServerEntry t = all.get(i);
            boolean active = t.id != null && t.id.trim().equals(activeId);
            boolean hover  = isHovering(b);
            ctx.fill(b.getX(), b.getY(), b.getX() + b.getWidth(),
                    b.getY() + b.getHeight(),
                    active ? C_SRV_ACTIVE_S : hover ? C_SRV_HOVER_S : C_SRV_IDLE);
            border(ctx, b.getX(), b.getY(), b.getWidth(), b.getHeight(),
                    active ? 0xFF6644CC : C_BORDER);
            if (active)
                ctx.fill(b.getX() + 1, b.getY(),
                        b.getX() + b.getWidth() - 1, b.getY() + 2, 0xFFAA66FF);
            ctx.drawText(textRenderer, Text.literal(t.displayName()),
                    b.getX() + 10,
                    b.getY() + (b.getHeight() - textRenderer.fontHeight) / 2,
                    active ? 0xFFFFEE44 : hover ? 0xFFCCDDFF : C_DIM, true);
        }

        if (modeBtn != null) {
            boolean canToggle = cachedActiveSrv != null
                    && cachedActiveSrv.hasWarp && cachedActiveSrv.hasHome;
            String lbl = !canToggle
                    ? (cachedActiveSrv != null && cachedActiveSrv.hasWarp
                    ? "仅 warp" : cachedActiveSrv != null && cachedActiveSrv.hasHome
                    ? "仅 home" : "无传送")
                    : showBothModes ? "▣ warp+home" : "▢ 仅 warp";
            boolean hover = isHovering(modeBtn);
            int mBg = !canToggle ? 0xFF0D1020 : hover ? 0xFF1E3A5A
                    : showBothModes ? 0xFF14305A : 0xFF1A1A30;
            int mFg = !canToggle ? 0xFF444455 : showBothModes ? 0xFF88CCFF : 0xFF8899AA;
            int mBd = !canToggle ? C_BORDER : showBothModes ? 0xFF4466AA : C_BORDER;
            ctx.fill(modeBtn.getX(), modeBtn.getY(),
                    modeBtn.getX() + modeBtn.getWidth(),
                    modeBtn.getY() + modeBtn.getHeight(), mBg);
            border(ctx, modeBtn.getX(), modeBtn.getY(),
                    modeBtn.getWidth(), modeBtn.getHeight(), mBd);
            if (canToggle && showBothModes)
                ctx.fill(modeBtn.getX() + 1, modeBtn.getY(),
                        modeBtn.getX() + modeBtn.getWidth() - 1,
                        modeBtn.getY() + 2, 0xFF4488CC);
            ctx.drawText(textRenderer, Text.literal(lbl),
                    modeBtn.getX() + 6,
                    modeBtn.getY() + (modeBtn.getHeight() - textRenderer.fontHeight) / 2,
                    mFg, true);
            modeBtn.active = canToggle;
        }
    }

    private void renderStylizedTabs(DrawContext ctx) {
        int tabY = pY + toolbarH + serverBarH;
        if (!showBothModes) {
            int fullW = pW - 16;
            ctx.fill(pX + 8, tabY, pX + 8 + fullW, tabY + tabH, C_TAB_ON);
            border(ctx, pX + 8, tabY, fullW, tabH, C_BORDER);
            String lbl = warpMode == WarpListManager.Mode.WARP
                    ? "共享传送点 (warp)" + (cachedWarpCount > 0 ? " " + cachedWarpCount : "")
                    : "个人传送点 (home)" + (cachedHomeCount > 0 ? " " + cachedHomeCount : "");
            ctx.drawText(textRenderer, Text.literal(lbl),
                    pX + 8 + (fullW - textRenderer.getWidth(lbl)) / 2,
                    tabY + (tabH - textRenderer.fontHeight) / 2, C_ACCENT, true);
            return;
        }
        int tabW = (pW - 20) / 2;
        boolean wa = warpMode == WarpListManager.Mode.WARP;
        ctx.fill(pX + 8,        tabY, pX + 8 + tabW,   tabY + tabH,
                wa ? C_TAB_ON : C_TAB_OFF);
        ctx.fill(pX + 8 + tabW + 4, tabY, pX + pW - 8, tabY + tabH,
                wa ? C_TAB_OFF : C_TAB_ON);
        border(ctx, pX + 8,        tabY, tabW,     tabH, C_BORDER);
        border(ctx, pX + 8 + tabW + 4, tabY, tabW - 4, tabH, C_BORDER);
        String wl = "共享传送点 (warp)" + (cachedWarpCount > 0 ? " " + cachedWarpCount : "");
        String hl = "个人传送点 (home)" + (cachedHomeCount > 0 ? " " + cachedHomeCount : "");
        int ty = tabY + (tabH - textRenderer.fontHeight) / 2;
        int wFg = wa ? C_ACCENT
                : (cachedActiveSrv != null && cachedActiveSrv.hasWarp ? C_DIM : 0xFF333344);
        int hFg = !wa ? C_ACCENT
                : (cachedActiveSrv != null && cachedActiveSrv.hasHome ? C_DIM : 0xFF333344);
        ctx.drawText(textRenderer, Text.literal(wl),
                pX + 8 + (tabW - textRenderer.getWidth(wl)) / 2, ty, wFg, true);
        ctx.drawText(textRenderer, Text.literal(hl),
                pX + 8 + tabW + 4 + (tabW - 4 - textRenderer.getWidth(hl)) / 2,
                ty, hFg, true);
        if (cachedActiveSrv != null && !cachedActiveSrv.hasWarp)
            ctx.fill(pX + 8, tabY, pX + 8 + tabW, tabY + tabH, 0x88000000);
        if (cachedActiveSrv != null && !cachedActiveSrv.hasHome)
            ctx.fill(pX + 8 + tabW + 4, tabY, pX + pW - 8, tabY + tabH, 0x88000000);
    }

    private void renderStylizedColHeader(DrawContext ctx) {
        ctx.fill(listX, listY, listX + listW, listY + colH, C_COL_HEAD);
        border(ctx, listX, listY, listW, colH, C_BORDER);
        int fy = listY + (colH - textRenderer.fontHeight) / 2;
        int x  = listX + 2;
        ctx.drawText(textRenderer, Text.literal("#"),    x, fy, C_DIM, true); x += colNumW;
        ctx.drawText(textRenderer, Text.literal("名称"), x, fy, C_DIM, true); x += colNameW;
        ctx.drawText(textRenderer, Text.literal("注释"), x, fy, C_DIM, true); x += colCommentW;
        ctx.drawText(textRenderer, Text.literal("日期"), x, fy, C_DIM, true);
    }

    private void renderStylizedRows(DrawContext ctx) {
        int ps = guiPage * itemsPerPage;
        ctx.enableScissor(listX, listY + colH, listX + listW, listY + listH);
        int fh = textRenderer.fontHeight;

        for (int i = 0; i < itemsPerPage; i++) {
            int idx = ps + i;
            if (idx >= filtered.size()) break;
            WarpEntry e  = filtered.get(idx);
            int iy       = rowBtnY0 + i * rowH;
            boolean sel  = idx == selIdx;
            boolean hov  = mouseX >= listX && mouseX < listX + listW
                        && mouseY >= iy && mouseY < iy + rowH;

            ctx.fill(listX, iy, listX + listW, iy + rowH,
                    sel ? C_ROW_SEL : hov ? C_ROW_HOVER
                        : (i % 2 == 0 ? C_ROW_EVEN : C_ROW_ODD_S));

            if (i > 0) ctx.fill(listX + 2, iy, listX + listW - 2, iy + 1, C_SEP);

            int fy = iy + (rowH - fh) / 2;
            int x  = listX + 2;

            // 序号列
            ctx.drawText(textRenderer, Text.literal(String.valueOf(idx + 1)),
                    x, fy, e.starred ? C_STAR_ON : C_NUM, true);
            x += colNumW;

            ctx.drawText(textRenderer,
                    Text.literal(truncate(e.name, colNameW - 4)),
                    x, fy, sel ? C_NAME_SEL : C_NAME, true);
            x += colNameW;

            if (e.hasComment())
                ctx.drawText(textRenderer,
                        Text.literal(truncate(e.comment, colCommentW - 4)),
                        x, fy, sel ? 0xFFCCDDEE : C_COMMENT, true);
            x += colCommentW;

            if (!e.date.isEmpty())
                ctx.drawText(textRenderer, Text.literal(e.shortDate()),
                        x, fy, C_DATE, true);

            // 传送 / 星标按钮（有独立背景）
            int tpX = listX + listW - colBtnW + 2;
            int stX = tpX + 38;
            boolean th = hov && mouseX >= tpX && mouseX < tpX + 34;
            boolean sh = hov && mouseX >= stX  && mouseX < stX  + 24;

            ctx.fill(tpX, iy + 2, tpX + 34, iy + rowH - 2,
                    th ? 0xFF205080 : C_BTN_TP);
            border(ctx, tpX, iy + 2, 34, rowH - 4, C_BORDER);
            Text tpLabel = Text.literal("传送");
            ctx.drawText(textRenderer, tpLabel,
                    tpX + (34 - textRenderer.getWidth(tpLabel)) / 2,
                    iy + (rowH - fh) / 2, 0xFFCCEEFF, true);

            ctx.fill(stX, iy + 2, stX + 24, iy + rowH - 2,
                    sh ? 0xFF333344 : C_BTN_STAR);
            border(ctx, stX, iy + 2, 24, rowH - 4,
                    e.starred ? 0xFF886600 : C_BORDER);
            String star = e.starred ? "★" : "☆";
            ctx.drawText(textRenderer, Text.literal(star),
                    stX + (24 - textRenderer.getWidth(star)) / 2,
                    iy + (rowH - fh) / 2,
                    e.starred ? C_STAR_ON : sh ? 0xFFBBBBBB : C_STAR_OFF, true);
        }
        ctx.disableScissor();
    }

    private void renderStylizedPageBar(DrawContext ctx) {
        int barY = listY + listH;
        ctx.fill(listX, barY, listX + listW, barY + footerH, C_PAGE_BAR);
        border(ctx, listX, barY, listW, footerH, C_BORDER);
        int mx2 = Math.max(0, (filtered.size() - 1) / itemsPerPage);
        String info = "第 " + (guiPage + 1) + " / " + (mx2 + 1)
                + " 页    共 " + filtered.size() + " 个传送点";
        ctx.drawText(textRenderer, Text.literal(info),
                listX + (listW - textRenderer.getWidth(info)) / 2,
                barY + (footerH - textRenderer.fontHeight) / 2, C_DIM, true);
    }

    // ── 工具 ─────────────────────────────────────────────────────

    /**
     * 用多层 fill 模拟像素级圆角矩形（仅用于 light/dark 主题）。
     * r=4 阶梯：row0 indent=3, row1 indent=1
     * r=6 阶梯：row0 indent=4, row1 indent=2, row2 indent=1
     * r=8 阶梯：row0 indent=5, row1 indent=3, row2 indent=2, row3 indent=1
     *
     * @param corners 控制哪些角圆化：TL=1, TR=2, BR=4, BL=8（可 OR 组合）
     */
    private void roundFill(DrawContext ctx, int x, int y, int w, int h, int color, int r, int corners) {
        if (w <= 0 || h <= 0) return;
        // 先填整个矩形
        ctx.fill(x, y, x + w, y + h, color);
        // 用透明像素逐行切去四角
        int[][] steps;
        if      (r <= 3) steps = new int[][]{{0,3},{1,1}};
        else if (r <= 5) steps = new int[][]{{0,4},{1,2},{2,1}};
        else if (r <= 7) steps = new int[][]{{0,5},{1,3},{2,2},{3,1}};
        else             steps = new int[][]{{0,5},{1,3},{2,2},{3,1},{4,1}};

        for (int[] s : steps) {
            int row = s[0], ind = s[1];
            // 顶部两行
            if ((corners & 1) != 0) ctx.fill(x,         y + row, x + ind,     y + row + 1, 0); // TL
            if ((corners & 2) != 0) ctx.fill(x+w-ind,   y + row, x + w,       y + row + 1, 0); // TR
            // 底部两行（镜像）
            if ((corners & 8) != 0) ctx.fill(x,         y+h-1-row, x + ind,   y+h-row,     0); // BL
            if ((corners & 4) != 0) ctx.fill(x+w-ind,   y+h-1-row, x + w,     y+h-row,     0); // BR
        }
    }

    /** 四角全圆 */
    private void roundFill(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        roundFill(ctx, x, y, w, h, color, r, 0xF);
    }

    /** 圆角矩形边框（先画边框色背景，再画内容色，再切角） */
    private void roundRect(DrawContext ctx, int x, int y, int w, int h,
                           int borderColor, int fillColor, int r) {
        roundFill(ctx, x-1, y-1, w+2, h+2, borderColor, r+1);
        roundFill(ctx, x,   y,   w,   h,   fillColor,   r);
    }

    /** 仅顶部两角圆化（用于 toolbar，底部接 tab 区） */
    private void roundFillTop(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        roundFill(ctx, x, y, w, h, color, r, 0x3); // TL+TR
    }

    /** 仅底部两角圆化（用于 footer） */
    private void roundFillBottom(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        roundFill(ctx, x, y, w, h, color, r, 0xC); // BL+BR
    }

    private boolean isHovering(ButtonWidget b) {
        return mouseX >= b.getX() && mouseX < b.getX() + b.getWidth()
            && mouseY >= b.getY() && mouseY < b.getY() + b.getHeight();
    }

    private String truncate(String s, int maxW) {
        if (textRenderer.getWidth(s) <= maxW) return s;
        int lo = 0, hi = s.length() - 1;
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (textRenderer.getWidth(s.substring(0, mid) + "…") <= maxW) lo = mid;
            else hi = mid - 1;
        }
        return s.substring(0, lo) + "…";
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x, y, x + w, y + 1, c);
        ctx.fill(x, y + h - 1, x + w, y + h, c);
        ctx.fill(x, y, x + 1, y + h, c);
        ctx.fill(x + w - 1, y, x + w, y + h, c);
    }

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (vAmount < 0) changePage(+1);
        else if (vAmount > 0) changePage(-1);
        return true;
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() {
        WarpListManager.getInstance().stopCapturing(); super.close();
    }
}
