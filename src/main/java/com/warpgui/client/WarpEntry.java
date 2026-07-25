package com.warpgui.client;

/**
 * 一条传送点记录
 */
public class WarpEntry {
    public final String  name;
    public final String  comment;
    public final String  date;       // yyyy-MM-dd 创建日期
    public final boolean isHome;     // true=个人传送点(home), false=共享(warp)
    public       boolean starred;    // 收藏标记（本地持久化）

    public WarpEntry(String name, String comment, String date, boolean isHome) {
        this.name    = name;
        this.comment = comment != null ? comment.trim() : "";
        this.date    = date    != null ? date.trim()    : "";
        this.isHome  = isHome;
        this.starred = false;
    }

    public boolean hasComment() { return !comment.isEmpty(); }
    public String shortDate() {
        return date.length() >= 7 ? date.substring(5) : date;
    }
}
