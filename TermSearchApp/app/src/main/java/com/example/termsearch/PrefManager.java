package com.example.termsearch;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 偏好设置：夜间模式、排序方式、当前抽屉选中项
 */
public class PrefManager {
    private static final String PREF_NAME = "term_search_pref";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_SORT = "sort_mode";
    private static final String KEY_DRAWER_MODE = "drawer_mode";
    private static final String KEY_DRAWER_CATEGORY = "drawer_category";

    public static final String MODE_ALL = "all";
    public static final String MODE_FAV = "fav";
    public static final String MODE_HISTORY = "history";
    public static final String MODE_CATEGORY = "category";

    private final SharedPreferences sp;

    public PrefManager(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isNightMode() {
        return sp.getBoolean(KEY_NIGHT_MODE, false);
    }

    public void setNightMode(boolean on) {
        sp.edit().putBoolean(KEY_NIGHT_MODE, on).apply();
    }

    public String getSortMode() {
        return sp.getString(KEY_SORT, DatabaseHelper.SORT_TIME_DESC);
    }

    public void setSortMode(String mode) {
        sp.edit().putString(KEY_SORT, mode).apply();
    }

    public String getDrawerMode() {
        return sp.getString(KEY_DRAWER_MODE, MODE_ALL);
    }

    public void setDrawerMode(String mode) {
        sp.edit().putString(KEY_DRAWER_MODE, mode).apply();
    }

    public String getDrawerCategory() {
        return sp.getString(KEY_DRAWER_CATEGORY, "");
    }

    public void setDrawerCategory(String category) {
        sp.edit().putString(KEY_DRAWER_CATEGORY, category).apply();
    }
}
