package com.warpgui.client;

public class WarpEntry {
    public final String  name;
    public final String  comment;
    public final String  date;
    public final boolean isHome;
    public       boolean starred;

    public WarpEntry(String name, String comment, String date, boolean isHome) {
        this.name    = name    != null ? name.trim()    : "";
        this.comment = comment != null ? comment.trim() : "";
        this.date    = date    != null ? date.trim()    : "";
        this.isHome  = isHome;
    }

    public boolean hasComment() { return !comment.isEmpty(); }

    /** 只取 MM-dd，避免每帧重复 substring */
    public String shortDate() {
        return date.length() >= 7 ? date.substring(5) : date;
    }
}
