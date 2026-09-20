package com.falcon.car.data;

import android.content.Context;
import android.content.SharedPreferences;

/** The few settings that outlive a session. */
public final class AppPrefs {

    private static final String FILE = "redline_prefs";
    private static final String KEY_DEMO_MODE = "demo_mode";

    private AppPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /**
     * Demo mode answers every adapter command from canned data. On by default
     * so a fresh install is explorable before any hardware is paired.
     */
    public static boolean isDemoMode(Context context) {
        return prefs(context).getBoolean(KEY_DEMO_MODE, true);
    }

    public static void setDemoMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DEMO_MODE, enabled).apply();
    }
}
