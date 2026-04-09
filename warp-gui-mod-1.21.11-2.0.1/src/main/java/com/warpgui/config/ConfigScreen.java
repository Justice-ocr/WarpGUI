package com.warpgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * WarpGUI 配置界面 — 三主题版（light / dark / stylized）
 */
public class ConfigScreen extends Screen {

    // ════════════════════════════════════════════════════════════
    // 色盘 — Light
    // ════════════════════════════════════════════════════════════
    private static final int L_SCRIM      = 0x55000000;
    private static final int L_PANEL      = 0xFFF5F7FA;
    private static final int L_TOOLBAR    = 0xFFEBEEF3;
    private static final int L_SEARCH_BG  = 0xFFFFFFFF;
    private static final int L_BORDER     = 0xFFCDD3DC;
    private static final int L_DIVIDER    = 0x18000000;
    private static final int L_ACCENT     = 0xFF4A90D9;
    private static final int L_ROW_SEL    = 0xFFDCE9F7;
    private static final int L_ROW_HOVER  = 0xFFEEF4FB;
    private static final int L_TEXT       = 0xFF1A2330;
    private static final int L_MUTED      = 0xFF6B7A8D;
    private static final int L_DISABLED   = 0xFFB8C4D0;
    private static final int L_OK         = 0xFF2E8B57;
    // 主题按钮（light → 按钮显示深色，方便区分）
    private static final int L_TBTN_BG    = 0xFF1A2330;
    private static final int L_TBTN_FG    = 0xFFEEF4FB;

    // ════════════════════════════════════════════════════════════
    // 色盘 — Dark
    // ════════════════════════════════════════════════════════════
    private static final int D_SCRIM      = 0x88000000;
    private static final int D_PANEL      = 0xFF111820;
    private static final int D_TOOLBAR    = 0xFF182230;
    private static final int D_SEARCH_BG  = 0xFF0C1018;
    private static final int D_BORDER     = 0xFF2A3A50;
    private static final int D_DIVIDER    = 0x28AABBCC;
    private static final int D_ACCENT     = 0xFF3D6080;
    private static final int D_ROW_SEL    = 0x44558899;
    private static final int D_ROW_HOVER  = 0x22AABBCC;
    private static final int D_TEXT       = 0xFFCCDDEE;
    private static final int D_MUTED      = 0xFF6A8099;
    private static final int D_DISABLED   = 0xFF3A4A5A;
    private static final int D_OK         = 0xFF44BB77;
    private static final int D_TBTN_BG    = 0xFF1A4070;
    private static final int D_TBTN_FG    = 0xFFCCDDFF;

    // ════════════════════════════════════════════════════════════
    // 色盘 — Stylized
    // ════════════════════════════════════════════════════════════
    private static final int S_SCRIM      = 0x70000000;
    private static final int S_PANEL      = 0xF01A1A2E;
    private static final int S_TOOLBAR    = 0xFF111830;
    private static final int S_SEARCH_BG  = 0xFF0C1020;
    private static final int S_BORDER     = 0xFF4A2D80;
    private static final int S_DIVIDER    = 0x20AAAAFF;
    private static final int S_ACCENT     = 0xFFFF6B9D;
    private static final int S_ROW_SEL    = 0xBBD04060;
    private static final int S_ROW_HOVER  = 0x990F3060;
    private static final int S_TEXT       = 0xFFDDEEFF;
    private static final int S_MUTED      = 0xFF8090A8;
    private static final int S_DISABLED   = 0xFF404055;
    private static final int S_OK         = 0xFF88FFAA;
    private static final int S_TBTN_BG    = 0xFF0F3460;
    private static final int S_TBTN_FG    = 0xFFFF6B9D;

    // ════════════════════════════════════════════════════════════
    // 运行时色盘指针
    // ════════════════════════════════════════════════════════════
    private int C_SCRIM, C_PANEL, C_TOOLBAR, C_SEARCH_BG, C_BORDER, C_DIVIDER;
    private int C_ACCENT, C_ROW_SEL, C_ROW_HOVER;
    private int C_TEXT, C_MUTED, C_DISABLED, C_OK;
    private int C_TBTN_BG, C_TBTN_FG;
    private boolean useShadow; // stylized 主题文字带阴影

    private void applyTheme() {
        String t = WarpConfig.get().uiTheme;
        boolean light = "light".equals(t);
        boolean dark  = "dark".equals(t);
        boolean sty   = !light && !dark; // stylized + fallback
        useShadow = sty;

        if (light) {
            C_SCRIM     = L_SCRIM;    C_PANEL    = L_PANEL;
            C_TOOLBAR   = L_TOOLBAR;  C_SEARCH_BG= L_SEARCH_BG;
            C_BORDER    = L_BORDER;   C_DIVIDER  = L_DIVIDER;
            C_ACCENT    = L_ACCENT;
            C_ROW_SEL   = L_ROW_SEL;  C_ROW_HOVER= L_ROW_HOVER;
            C_TEXT      = L_TEXT;     C_MUTED    = L_MUTED;
            C_DISABLED  = L_DISABLED; C_OK       = L_OK;
            C_TBTN_BG   = L_TBTN_BG;  C_TBTN_FG  = L_TBTN_FG;
        } else if (dark) {
            C_SCRIM     = D_SCRIM;    C_PANEL    = D_PANEL;
            C_TOOLBAR   = D_TOOLBAR;  C_SEARCH_BG= D_SEARCH_BG;
            C_BORDER    = D_BORDER;   C_DIVIDER  = D_DIVIDER;
            C_ACCENT    = D_ACCENT;
            C_ROW_SEL   = D_ROW_SEL;  C_ROW_HOVER= D_ROW_HOVER;
            C_TEXT      = D_TEXT;     C_MUTED    = D_MUTED;
            C_DISABLED  = D_DISABLED; C_OK       = D_OK;
            C_TBTN_BG   = D_TBTN_BG;  C_TBTN_FG  = D_TBTN_FG;
        } else {
            C_SCRIM     = S_SCRIM;    C_PANEL    = S_PANEL;
            C_TOOLBAR   = S_TOOLBAR;  C_SEARCH_BG= S_SEARCH_BG;
            C_BORDER    = S_BORDER;   C_DIVIDER  = S_DIVIDER;
            C_ACCENT    = S_ACCENT;
            C_ROW_SEL   = S_ROW_SEL;  C_ROW_HOVER= S_ROW_HOVER;
            C_TEXT      = S_TEXT;     C_MUTED    = S_MUTED;
            C_DISABLED  = S_DISABLED; C_OK       = S_OK;
            C_TBTN_BG   = S_TBTN_BG;  C_TBTN_FG  = S_TBTN_FG;
        }
    }

    // ── 布局常量 ─────────────────────────────────────────────────
    private static final int TOOLBAR_H  = 30;
    private static final int TAB_H      = 24;
    private static final int PAD        = 12;
    private static final int LABEL_H    = 10;
    private static final int FIELD_H    = 20;
    private static final int ROW_H      = LABEL_H + FIELD_H + 8;
    private static final int SRV_LIST_W = 104;
    private static final int SRV_ITEM_H = 18;
    private static final int TBTN_W     = 96;
    private static final int TBTN_H     = 20;

    private final Screen parent;
    private int tab = 0;

    private TextFieldWidget fWarpList, fHomeList, fWarpTp, fHomeTp, fSwitchSrv, fPageDelay;

    private int selSrv = -1;
    private TextFieldWidget fSrvName, fSrvId, fSrvKeywords, fSrvAutoRefresh;
    private final List<ButtonWidget> srvItemBtns = new ArrayList<>();

    private int pX, pY, pW, pH;
    private String statusMsg  = "";
    private int    statusTick = 0;

    // ── 坐标辅助 ─────────────────────────────────────────────────
    private int contentY()     { return pY + TOOLBAR_H + TAB_H + PAD; }
    private int cmdRowY(int n) { return contentY() + n * ROW_H; }
    private int srvBtnAreaY()  { return contentY(); }
    private int srvListY()     { return srvBtnAreaY() + 24; }
    private int srvRowY(int n) { return srvListY() + n * ROW_H; }
    private int editX()        { return pX + PAD + SRV_LIST_W + PAD; }
    private int editW()        { return pW - PAD * 2 - SRV_LIST_W - PAD; }
    // 主题按钮：跟随最后一个指令字段下方，避免被字段遮住
    private int tBtnX() { return pX + pW - PAD - TBTN_W; }
    private int tBtnY() {
        // 最后一个字段（index=5）底部 + 间距
        int lastFieldBottom = cmdRowY(5) + LABEL_H + FIELD_H;
        return lastFieldBottom + 10;
    }

    public ConfigScreen(Screen parent) {
        super(Text.literal("WarpGUI 配置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        applyTheme();
        pW = Math.min(width - 40, 560);
        // 高度：toolbar + tab + 内容 + 主题按钮区 + 状态行，但不超过屏幕
        int contentH = TOOLBAR_H + TAB_H + PAD
                + 6 * ROW_H          // 6个指令字段
                + TBTN_H + 10 + 24   // 主题按钮 + 状态消息行
                + PAD;
        pH = clamp(contentH, 360, height - 40);
        pX = (width  - pW) / 2;
        pY = (height - pH) / 2;
        buildTab();
    }

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    // ── 工具栏/Tab 区 hotspot 引用（用于自定义渲染）──────────────
    private ButtonWidget cfgCloseBtn, cfgSaveBtn;
    private ButtonWidget cfgTab0Btn, cfgTab1Btn;
    private final List<ButtonWidget> cfgSrvOpBtns = new ArrayList<>(); // +↑↓✕

    private void buildTab() {
        applyTheme();
        clearChildren();
        srvItemBtns.clear();
        cfgSrvOpBtns.clear();

        // 工具栏：✕ 和 保存 → 透明 hotspot，自定义渲染
        int iconH = TOOLBAR_H - 10, iconY = pY + 5;
        cfgCloseBtn = mkHotspot(pX + pW - 8 - 20,       iconY, 20, iconH, this::close);
        cfgSaveBtn  = mkHotspot(pX + pW - 8 - 20 - 4 - 46, iconY, 46, iconH, this::save);

        // Tab 按钮 → 透明 hotspot，自定义渲染
        int tabY = pY + TOOLBAR_H;
        int tabW = (pW - PAD * 2) / 2;
        cfgTab0Btn = mkHotspot(pX + PAD,            tabY, tabW - 1,                TAB_H, () -> switchTab(0));
        cfgTab1Btn = mkHotspot(pX + PAD + tabW + 1, tabY, pW - PAD * 2 - tabW - 1, TAB_H, () -> switchTab(1));

        if (tab == 0) buildCommandsTab();
        else          buildServersTab();
    }

    private void buildCommandsTab() {
        WarpConfig cfg = WarpConfig.get();
        int x = pX + PAD, w = pW - PAD * 2;
        fWarpList  = addField(x, cmdRowY(0), w, cfg.commands.warpList,                      "warp list {page}");
        fHomeList  = addField(x, cmdRowY(1), w, cfg.commands.homeList,                      "home list {page}");
        fWarpTp    = addField(x, cmdRowY(2), w, cfg.commands.warpTp,                        "warp tp {name}");
        fHomeTp    = addField(x, cmdRowY(3), w, cfg.commands.homeTp,                        "home tp {name}");
        fSwitchSrv = addField(x, cmdRowY(4), w, cfg.commands.switchServer,                  "server {id}");
        fPageDelay = addField(x, cmdRowY(5), w, String.valueOf(cfg.commands.pageDelayTicks), "1–100 tick");

        // 三态主题切换按钮（右下角）→ hotspot，自定义渲染在 render() 中进行
        mkHotspot(tBtnX(), tBtnY(), TBTN_W, TBTN_H, this::toggleTheme);
    }

    private void buildServersTab() {
        WarpConfig cfg = WarpConfig.get();
        int bW = 26, bY = srvBtnAreaY(), bX = pX + PAD;
        cfgSrvOpBtns.clear();
        cfgSrvOpBtns.add(mkHotspot(bX,             bY, bW, 20, this::addSrv));
        cfgSrvOpBtns.add(mkHotspot(bX + bW + 3,   bY, bW, 20, () -> moveSrv(-1)));
        cfgSrvOpBtns.add(mkHotspot(bX + bW*2 + 6, bY, bW, 20, () -> moveSrv(1)));
        cfgSrvOpBtns.add(mkHotspot(bX + bW*3 + 9, bY, bW, 20, this::delSrv));

        int listX = pX + PAD, listY = srvListY();
        for (int i = 0; i < cfg.servers.size(); i++) {
            final int fi = i;
            srvItemBtns.add(mkHotspot(listX + 1, listY + 2 + i * SRV_ITEM_H,
                    SRV_LIST_W - 2, SRV_ITEM_H, () -> { selSrv = fi; buildTab(); }));
        }

        if (selSrv >= 0 && selSrv < cfg.servers.size()) {
            WarpConfig.ServerEntry e = cfg.servers.get(selSrv);
            int ex = editX(), ew = editW();
            fSrvName        = addField(ex, srvRowY(0), ew, e.name,                            "如：生电服务器");
            fSrvId          = addField(ex, srvRowY(1), ew, e.id,                              "如：shengdian");
            fSrvKeywords    = addField(ex, srvRowY(2), ew, String.join(",", e.keywords),       "逗号分隔关键词");
            fSrvAutoRefresh = addField(ex, srvRowY(3), ew, String.valueOf(e.autoRefreshHours), "小时，0=禁用");
            mkBtn("warp " + (e.hasWarp ? "✔" : "✘"), ex,      srvRowY(4) + LABEL_H, 66, FIELD_H, this::toggleWarp);
            mkBtn("home " + (e.hasHome ? "✔" : "✘"), ex + 72, srvRowY(4) + LABEL_H, 66, FIELD_H, this::toggleHome);
        }
    }

    /** 根据当前主题决定按钮标签（显示切换到哪个主题）*/
    private static String themeLabel(String current) {
        return switch (current) {
            case "light"    -> "☾  切换为深色";
            case "dark"     -> "✦  切换为风格化";
            default         -> "☀  切换为明亮";  // stylized → light
        };
    }

    /** 三态循环：light → dark → stylized → light */
    private void toggleTheme() {
        saveCommandsFields();
        WarpConfig cfg = WarpConfig.get();
        cfg.uiTheme = switch (cfg.uiTheme) {
            case "light"  -> "dark";
            case "dark"   -> "stylized";
            default       -> "light";
        };
        cfg.save();
        applyTheme();
        buildTab();
        statusMsg = switch (cfg.uiTheme) {
            case "light"    -> "☀  已切换为明亮模式";
            case "dark"     -> "☾  已切换为深色模式";
            default         -> "✦  已切换为风格化模式";
        };
        statusTick = 80;
    }

    private TextFieldWidget addField(int x, int rowY, int w, String value, String placeholder) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, rowY + LABEL_H, w, FIELD_H,
                Text.literal(""));
        f.setMaxLength(200);
        f.setText(value != null ? value : "");
        f.setPlaceholder(Text.literal(placeholder));
        f.setDrawsBackground(false); // 禁用默认黑色背景，使用主题色自定义背景
        addSelectableChild(f);
        addDrawableChild(f);
        return f;
    }

    private ButtonWidget mkBtn(String lbl, int x, int y, int w, int h, Runnable r) {
        return addDrawableChild(ButtonWidget.builder(Text.literal(lbl), $ -> r.run())
                .dimensions(x, y, w, h).build());
    }
    private ButtonWidget mkBtn(String lbl, int x, int y, int w, Runnable r) {
        return mkBtn(lbl, x, y, w, 16, r);
    }
    private ButtonWidget mkHotspot(int x, int y, int w, int h, Runnable r) {
        ButtonWidget b = ButtonWidget.builder(Text.empty(), $ -> r.run())
                .dimensions(x, y, w, h).build();
        b.setAlpha(0f);
        return addDrawableChild(b);
    }

    private void switchTab(int t) { tab = t; selSrv = -1; buildTab(); }

    private void addSrv() {
        WarpConfig.ServerEntry e = new WarpConfig.ServerEntry();
        e.name = "新服务器"; e.id = "new"; e.keywords = new ArrayList<>();
        e.hasWarp = true; e.hasHome = true;
        WarpConfig.get().servers.add(e);
        selSrv = WarpConfig.get().servers.size() - 1;
        buildTab();
    }
    private void delSrv() {
        List<WarpConfig.ServerEntry> s = WarpConfig.get().servers;
        if (selSrv >= 0 && selSrv < s.size()) {
            s.remove(selSrv);
            selSrv = Math.min(selSrv, s.size() - 1);
            buildTab();
        }
    }
    private void moveSrv(int d) {
        List<WarpConfig.ServerEntry> s = WarpConfig.get().servers;
        int t2 = selSrv + d;
        if (selSrv < 0 || t2 < 0 || t2 >= s.size()) return;
        WarpConfig.ServerEntry tmp = s.get(selSrv); s.set(selSrv, s.get(t2)); s.set(t2, tmp);
        selSrv = t2; buildTab();
    }
    private void toggleWarp() {
        if (selSrv < 0 || selSrv >= WarpConfig.get().servers.size()) return;
        WarpConfig.get().servers.get(selSrv).hasWarp ^= true; buildTab();
    }
    private void toggleHome() {
        if (selSrv < 0 || selSrv >= WarpConfig.get().servers.size()) return;
        WarpConfig.get().servers.get(selSrv).hasHome ^= true; buildTab();
    }

    private void saveCommandsFields() {
        if (tab != 0) return;
        WarpConfig cfg = WarpConfig.get();
        if (fWarpList  != null) cfg.commands.warpList     = fWarpList.getText().trim();
        if (fHomeList  != null) cfg.commands.homeList     = fHomeList.getText().trim();
        if (fWarpTp    != null) cfg.commands.warpTp       = fWarpTp.getText().trim();
        if (fHomeTp    != null) cfg.commands.homeTp       = fHomeTp.getText().trim();
        if (fSwitchSrv != null) cfg.commands.switchServer = fSwitchSrv.getText().trim();
        if (fPageDelay != null) try {
            cfg.commands.pageDelayTicks = Math.max(1, Math.min(100,
                    Integer.parseInt(fPageDelay.getText().trim())));
        } catch (NumberFormatException ignored) {}
    }

    private void save() {
        WarpConfig cfg = WarpConfig.get();
        if (tab == 0) {
            saveCommandsFields();
        } else if (selSrv >= 0 && selSrv < cfg.servers.size()) {
            WarpConfig.ServerEntry e = cfg.servers.get(selSrv);
            if (fSrvName        != null) e.name = fSrvName.getText().trim();
            if (fSrvId          != null) e.id   = fSrvId.getText().trim();
            if (fSrvKeywords    != null) {
                e.keywords = new ArrayList<>();
                for (String kw : fSrvKeywords.getText().split(","))
                    if (!kw.trim().isEmpty()) e.keywords.add(kw.trim());
            }
            if (fSrvAutoRefresh != null) try {
                e.autoRefreshHours = Math.max(0,
                        Integer.parseInt(fSrvAutoRefresh.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        cfg.save();
        statusMsg = "✔  已保存"; statusTick = 80;
    }

    // ── 渲染 ─────────────────────────────────────────────────────

    @Override public void renderBackground(DrawContext ctx, int mx, int my, float delta) {}

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, C_SCRIM);

        // 面板 — light/dark 用 r=8 圆角，stylized 保持直角带边框
        if (!useShadow) {
            roundRect(ctx, pX, pY, pW, pH, C_BORDER, C_PANEL, 8);
        } else {
            ctx.fill(pX - 1, pY - 1, pX + pW + 1, pY + pH + 1, C_BORDER);
            ctx.fill(pX, pY, pX + pW, pY + pH, C_PANEL);
        }

        // 工具栏 — light/dark 顶部两角 r=8
        if (!useShadow) {
            roundFillTop(ctx, pX, pY, pW, TOOLBAR_H, C_TOOLBAR, 8);
            roundFill(ctx, pX, pY, 3, TOOLBAR_H, C_ACCENT, 8, 0x1); // TL accent 竖条
        } else {
            ctx.fill(pX, pY, pX + pW, pY + TOOLBAR_H, C_TOOLBAR);
            border(ctx, pX, pY, pW, TOOLBAR_H, C_BORDER);
        }
        ctx.fill(pX, pY + TOOLBAR_H - 1, pX + pW, pY + TOOLBAR_H, C_BORDER);
        ctx.drawText(textRenderer, Text.literal("配置"),
                pX + 10, pY + (TOOLBAR_H - textRenderer.fontHeight) / 2,
                C_TEXT, useShadow);
        if (useShadow) border(ctx, pX, pY, pW, TOOLBAR_H, C_BORDER);

        // 工具栏右侧：保存 + ✕ 自定义渲染
        renderCfgToolBtn(ctx, mx, my, cfgSaveBtn,  "保存", false);
        renderCfgToolBtn(ctx, mx, my, cfgCloseBtn, "✕",   true);

        // Tab 栏
        int tabY = pY + TOOLBAR_H;
        int tabW = (pW - PAD * 2) / 2;
        ctx.fill(pX, tabY, pX + pW, tabY + TAB_H, C_PANEL);
        ctx.fill(pX, tabY + TAB_H - 1, pX + pW, tabY + TAB_H, C_BORDER);
        int ty = tabY + (TAB_H - textRenderer.fontHeight) / 2;

        // Tab 0: 指令设置
        boolean t0hov = cfgTab0Btn != null && isHoverXY(mx, my, cfgTab0Btn);
        int t0cx = pX + PAD + (tabW - 1 - textRenderer.getWidth("指令设置")) / 2;
        if (t0hov && tab != 0) ctx.fill(pX + PAD, tabY, pX + PAD + tabW - 1, tabY + TAB_H, C_ROW_HOVER);
        ctx.drawText(textRenderer, Text.literal("指令设置"), t0cx, ty,
                tab == 0 ? C_ACCENT : (t0hov ? C_TEXT : C_MUTED), useShadow);
        if (tab == 0) ctx.fill(t0cx, tabY + TAB_H - 2, t0cx + textRenderer.getWidth("指令设置"), tabY + TAB_H, C_ACCENT);

        // Tab 1: 服务器管理
        int t1w  = pW - PAD * 2 - tabW - 1;
        boolean t1hov = cfgTab1Btn != null && isHoverXY(mx, my, cfgTab1Btn);
        int t1cx = pX + PAD + tabW + 1 + (t1w - textRenderer.getWidth("服务器管理")) / 2;
        if (t1hov && tab != 1) ctx.fill(pX + PAD + tabW + 1, tabY, pX + pW - PAD, tabY + TAB_H, C_ROW_HOVER);
        ctx.drawText(textRenderer, Text.literal("服务器管理"), t1cx, ty,
                tab == 1 ? C_ACCENT : (t1hov ? C_TEXT : C_MUTED), useShadow);
        if (tab == 1) ctx.fill(t1cx, tabY + TAB_H - 2, t1cx + textRenderer.getWidth("服务器管理"), tabY + TAB_H, C_ACCENT);

        ctx.fill(pX + PAD + tabW, tabY + 5, pX + PAD + tabW + 1, tabY + TAB_H - 5, C_BORDER);

        if (tab == 0) renderCommandsTab(ctx, mx, my);
        else          renderServersTab(ctx, mx, my);

        // 状态消息（左下角）
        if (statusTick > 0) {
            statusTick--;
            ctx.drawText(textRenderer, Text.literal(statusMsg),
                    pX + PAD, pY + pH - PAD - textRenderer.fontHeight, C_OK, useShadow);
        }
        super.render(ctx, mx, my, delta);
    }

    private void renderCommandsTab(DrawContext ctx, int mx, int my) {
        String[][] rows = {
            {"warp 列表指令",  "{page} = 页码"},
            {"home 列表指令",  "{page} = 页码"},
            {"warp 传送指令",  "{name} = 传送点名"},
            {"home 传送指令",  "{name} = 传送点名"},
            {"切换服务器指令", "{id} = 服务器 ID"},
            {"翻页延迟",       "单位 tick（50ms），范围 1–100"},
        };
        int x = pX + PAD, fw = pW - PAD * 2;
        for (int i = 0; i < rows.length; i++) {
            int ry = cmdRowY(i);
            ctx.drawText(textRenderer, Text.literal(rows[i][0]), x, ry, C_TEXT, useShadow);
            ctx.drawText(textRenderer, Text.literal(rows[i][1]),
                    x + textRenderer.getWidth(rows[i][0]) + 6, ry, C_MUTED, useShadow);
            // 输入框背景（主题色，圆角）
            boolean focused = isFocused(i);
            if (!useShadow)
                roundRect(ctx, x, ry + LABEL_H, fw, FIELD_H,
                        focused ? C_ACCENT : C_BORDER, C_SEARCH_BG, 3);
            else {
                ctx.fill(x - 1, ry + LABEL_H - 1, x + fw + 1, ry + LABEL_H + FIELD_H + 1, C_BORDER);
                ctx.fill(x, ry + LABEL_H, x + fw, ry + LABEL_H + FIELD_H, C_SEARCH_BG);
            }
        }
        renderThemeButton(ctx, mx, my);
    }

    private boolean isFocused(int fieldIdx) {
        TextFieldWidget[] fields = {fWarpList, fHomeList, fWarpTp, fHomeTp, fSwitchSrv, fPageDelay};
        return fieldIdx < fields.length && fields[fieldIdx] != null && fields[fieldIdx].isFocused();
    }

    /** 手动绘制主题切换按钮（用主题色，视觉与面板融合） */
    private void renderThemeButton(DrawContext ctx, int mx, int my) {
        String lbl = themeLabel(WarpConfig.get().uiTheme);
        int bx = tBtnX(), by = tBtnY(), bw = TBTN_W, bh = TBTN_H;
        boolean hover = mx >= bx && mx < bx + bw && my >= by && my < by + bh;

        int bg = hover
                ? (useShadow ? 0xFF1A5090 : (C_TBTN_BG == L_TBTN_BG ? 0xFF0A1828 : 0xFFEEF4FB))
                : C_TBTN_BG;

        if (!useShadow) {
            // light/dark：r=4 胶囊形
            roundFill(ctx, bx, by, bw, bh, bg, 4);
        } else {
            ctx.fill(bx, by, bx + bw, by + bh, bg);
            border(ctx, bx, by, bw, bh, C_BORDER);
        }

        // 文字居中
        int lw = textRenderer.getWidth(lbl);
        ctx.drawText(textRenderer, Text.literal(lbl),
                bx + (bw - lw) / 2, by + (bh - textRenderer.fontHeight) / 2,
                C_TBTN_FG, useShadow);

        // 标签
        ctx.drawText(textRenderer, Text.literal("UI 风格"),
                bx, by - textRenderer.fontHeight - 2, C_MUTED, useShadow);
    }

    private void renderServersTab(DrawContext ctx, int mx, int my) {
        WarpConfig cfg = WarpConfig.get();
        int listX = pX + PAD, listY = srvListY();
        int listH = pH - (listY - pY) - PAD - 30;

        // 服务器操作按钮（＋ ↑ ↓ ✕）
        String[] opIcons = {"＋", "↑", "↓", "✕"};
        for (int i = 0; i < cfgSrvOpBtns.size() && i < opIcons.length; i++) {
            ButtonWidget b = cfgSrvOpBtns.get(i);
            boolean hover = isHoverXY(mx, my, b);
            boolean isDel = (i == 3);
            int bg = hover ? (isDel ? 0x44FF4444 : C_ROW_HOVER) : 0;
            if (!useShadow) {
                roundFill(ctx, b.getX()-1, b.getY()-1, b.getWidth()+2, b.getHeight()+2, C_BORDER, 3);
                roundFill(ctx, b.getX(), b.getY(), b.getWidth(), b.getHeight(),
                        bg != 0 ? bg : C_TOOLBAR, 3);
            } else {
                ctx.fill(b.getX()-1, b.getY()-1, b.getX()+b.getWidth()+1, b.getY()+b.getHeight()+1, C_BORDER);
                ctx.fill(b.getX(), b.getY(), b.getX()+b.getWidth(), b.getY()+b.getHeight(),
                        bg != 0 ? bg : 0xFF0C1020);
            }
            int fg = hover && isDel ? 0xFFFF6666 : hover ? C_ACCENT : C_TEXT;
            ctx.drawText(textRenderer, Text.literal(opIcons[i]),
                    b.getX() + (b.getWidth()  - textRenderer.getWidth(opIcons[i])) / 2,
                    b.getY() + (b.getHeight() - textRenderer.fontHeight) / 2,
                    fg, useShadow);
        }

        ctx.fill(listX - 1, listY - 1, listX + SRV_LIST_W + 1, listY + listH + 1, C_BORDER);
        ctx.fill(listX, listY, listX + SRV_LIST_W, listY + listH, C_SEARCH_BG);

        for (int i = 0; i < cfg.servers.size(); i++) {
            int iy = listY + 2 + i * SRV_ITEM_H;
            if (iy + SRV_ITEM_H > listY + listH) break;
            WarpConfig.ServerEntry e = cfg.servers.get(i);
            boolean sel   = i == selSrv;
            boolean hover = !sel && mx >= listX && mx < listX + SRV_LIST_W
                         && my >= iy && my < iy + SRV_ITEM_H;
            if (sel)
                ctx.fill(listX + 1, iy, listX + SRV_LIST_W - 1, iy + SRV_ITEM_H, C_ROW_SEL);
            else if (hover)
                ctx.fill(listX + 1, iy, listX + SRV_LIST_W - 1, iy + SRV_ITEM_H, C_ROW_HOVER);
            if (sel) ctx.fill(listX + 1, iy, listX + 4, iy + SRV_ITEM_H, C_ACCENT);
            if (i > 0) ctx.fill(listX + 4, iy, listX + SRV_LIST_W - 4, iy + 1, C_DIVIDER);
            ctx.drawText(textRenderer, Text.literal(e.displayName()),
                    listX + 8, iy + (SRV_ITEM_H - textRenderer.fontHeight) / 2,
                    sel ? C_ACCENT : C_TEXT, useShadow);
        }

        if (selSrv >= 0 && selSrv < cfg.servers.size()) {
            String[][] rows = {
                {"显示名称", "标签栏中显示的名称"},
                {"服务器ID", "/server {id} 参数"},
                {"关键词",   "检测切服消息，逗号分隔"},
                {"自动刷新", "间隔小时（0=禁用）"},
                {"传送功能", ""},
            };
            int ex = editX();
            for (int i = 0; i < rows.length; i++) {
                int ry = srvRowY(i);
                ctx.drawText(textRenderer, Text.literal(rows[i][0]), ex, ry, C_TEXT, useShadow);
                if (!rows[i][1].isEmpty())
                    ctx.drawText(textRenderer, Text.literal(rows[i][1]),
                            ex + textRenderer.getWidth(rows[i][0]) + 6, ry, C_MUTED, useShadow);
            }
        } else {
            ctx.drawText(textRenderer, Text.literal("← 选择服务器进行编辑"),
                    editX(), srvBtnAreaY() + 46, C_DISABLED, useShadow);
        }
    }

    // ── 配置界面自定义按钮渲染 ──────────────────────────────────────

    /** 工具栏按钮（保存 / ✕）：圆角背景 + 文字 */
    private void renderCfgToolBtn(DrawContext ctx, int mx, int my,
                                   ButtonWidget btn, String lbl, boolean isDanger) {
        if (btn == null) return;
        boolean hover = isHoverXY(mx, my, btn);
        int bg, fg;
        if (isDanger) {
            bg = hover ? (useShadow ? 0xFF3A1010 : 0xFFFFEEEE) : 0;
            fg = hover ? (useShadow ? 0xFFFF6666 : 0xFFCC2222) : C_MUTED;
        } else {
            bg = hover ? C_ROW_HOVER : 0;
            fg = hover ? C_ACCENT    : C_MUTED;
        }
        if (!useShadow) {
            if (bg != 0) roundFill(ctx, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), bg, 4);
            else {
                // 非hover时：细边框胶囊
                roundFill(ctx, btn.getX()-1, btn.getY()-1, btn.getWidth()+2, btn.getHeight()+2, C_BORDER, 4);
                roundFill(ctx, btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight(), C_TOOLBAR, 4);
            }
        } else {
            ctx.fill(btn.getX()-1, btn.getY()-1, btn.getX()+btn.getWidth()+1, btn.getY()+btn.getHeight()+1, C_BORDER);
            ctx.fill(btn.getX(), btn.getY(), btn.getX()+btn.getWidth(), btn.getY()+btn.getHeight(),
                    bg != 0 ? bg : C_SEARCH_BG);
        }
        int lw = textRenderer.getWidth(lbl);
        ctx.drawText(textRenderer, Text.literal(lbl),
                btn.getX() + (btn.getWidth()  - lw)                   / 2,
                btn.getY() + (btn.getHeight() - textRenderer.fontHeight) / 2,
                fg, useShadow);
    }

    private boolean isHoverXY(int mx, int my, ButtonWidget b) {
        return mx >= b.getX() && mx < b.getX() + b.getWidth()
            && my >= b.getY() && my < b.getY() + b.getHeight();
    }

    // ── 圆角工具（镜像 WarpGuiScreen，用于 light/dark 主题） ──────

    private void roundFill(DrawContext ctx, int x, int y, int w, int h, int color, int r, int corners) {
        if (w <= 0 || h <= 0) return;
        ctx.fill(x, y, x + w, y + h, color);
        int[][] steps;
        if      (r <= 3) steps = new int[][]{{0,3},{1,1}};
        else if (r <= 5) steps = new int[][]{{0,4},{1,2},{2,1}};
        else if (r <= 7) steps = new int[][]{{0,5},{1,3},{2,2},{3,1}};
        else             steps = new int[][]{{0,5},{1,3},{2,2},{3,1},{4,1}};
        for (int[] s : steps) {
            int row = s[0], ind = s[1];
            if ((corners & 1) != 0) ctx.fill(x,       y+row,     x+ind,   y+row+1,   0);
            if ((corners & 2) != 0) ctx.fill(x+w-ind, y+row,     x+w,     y+row+1,   0);
            if ((corners & 8) != 0) ctx.fill(x,       y+h-1-row, x+ind,   y+h-row,   0);
            if ((corners & 4) != 0) ctx.fill(x+w-ind, y+h-1-row, x+w,     y+h-row,   0);
        }
    }
    private void roundFill(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        roundFill(ctx, x, y, w, h, color, r, 0xF);
    }
    private void roundRect(DrawContext ctx, int x, int y, int w, int h, int bc, int fc, int r) {
        roundFill(ctx, x-1, y-1, w+2, h+2, bc, r+1);
        roundFill(ctx, x,   y,   w,   h,   fc, r);
    }
    private void roundFillTop(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        roundFill(ctx, x, y, w, h, color, r, 0x3);
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x, y, x + w, y + 1, c);
        ctx.fill(x, y + h - 1, x + w, y + h, c);
        ctx.fill(x, y, x + 1, y + h, c);
        ctx.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { client.setScreen(parent); }
}
