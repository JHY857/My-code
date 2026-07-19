package com.wikisearch.app.util;

import android.content.Context;
import android.content.res.Configuration;

import com.wikisearch.app.R;

public class ThemeUtils {

    public static void applyTheme(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        if (nightMode) {
            context.setTheme(R.style.AppTheme_Night);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    public static int getPrimaryColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_primary)
                : context.getResources().getColor(R.color.primary);
    }

    public static int getBackgroundColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_background)
                : context.getResources().getColor(R.color.background);
    }

    public static int getCardBackgroundColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_card)
                : context.getResources().getColor(R.color.card_background);
    }

    public static int getTextPrimaryColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_text_primary)
                : context.getResources().getColor(R.color.text_primary);
    }

    public static int getTextSecondaryColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_text_secondary)
                : context.getResources().getColor(R.color.text_secondary);
    }

    public static int getSearchBgColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_search_bg)
                : context.getResources().getColor(R.color.search_bg);
    }

    public static int getDividerColor(Context context) {
        boolean nightMode = PrefsManager.getInstance(context).isNightMode();
        return nightMode ? context.getResources().getColor(R.color.night_divider)
                : context.getResources().getColor(R.color.divider);
    }

    public static boolean isNightMode(Context context) {
        return PrefsManager.getInstance(context).isNightMode();
    }
}
