package com.wikisearch.app.model;

public class Category {
    private String name;
    private int iconResId;
    private int colorResId;
    private int count;

    public Category(String name, int iconResId, int colorResId) {
        this.name = name;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
    }

    public Category(String name, int iconResId, int colorResId, int count) {
        this.name = name;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
