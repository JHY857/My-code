package com.wikisearch.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREFS_NAME = "wikisearch_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_FONT_SIZE = "font_size";

    public static final int FONT_SMALL = 0;
    public static final int FONT_MEDIUM = 1;
    public static final int FONT_LARGE = 2;

    private static PrefsManager instance;
    private SharedPreferences prefs;

    private PrefsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrefsManager(context);
        }
        return instance;
    }

    public boolean isNightMode() {
        return prefs.getBoolean(KEY_NIGHT_MODE, false);
    }

    public void setNightMode(boolean nightMode) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE, nightMode).apply();
    }

    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, FONT_MEDIUM);
    }

    public void setFontSize(int fontSize) {
        prefs.edit().putInt(KEY_FONT_SIZE, fontSize).apply();
    }

    public int getContentFontSize() {
        int fontSize = getFontSize();
        switch (fontSize) {
            case FONT_SMALL:
                return 14;
            case FONT_LARGE:
                return 20;
            case FONT_MEDIUM:
            default:
                return 16;
        }
    }

    public int getTitleFontSize() {
        int fontSize = getFontSize();
        switch (fontSize) {
            case FONT_SMALL:
                return 18;
            case FONT_LARGE:
                return 28;
            case FONT_MEDIUM:
            default:
                return 24;
        }
    }
}
