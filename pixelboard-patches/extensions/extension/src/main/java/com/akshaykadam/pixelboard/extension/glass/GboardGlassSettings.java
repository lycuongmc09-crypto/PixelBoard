/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.glass;

import android.content.Context;
import android.content.SharedPreferences;

import com.akshaykadam.pixelboard.extension.settings.GboardPatchesSettings;

/**
 * Preferences for the experimental liquid-glass toolbar overlay.
 *
 * <p>The feature is opt-in because glass rendering has a real GPU cost on
 * low-end devices. Phase 1 draws a static frosted scrim with a specular rim;
 * later phases add backdrop blur and refraction.</p>
 */
public final class GboardGlassSettings {
    public static final String PREF_KEY_ENABLED = "glass_overlay_enabled";
    public static final boolean DEFAULT_ENABLED = false;

    private static volatile Boolean cachedEnabled;

    private GboardGlassSettings() {
    }

    public static SharedPreferences preferences(Context context) {
        return GboardPatchesSettings.preferences(context);
    }

    public static boolean readEnabled(SharedPreferences preferences) {
        if (preferences == null) {
            return DEFAULT_ENABLED;
        }
        return preferences.getBoolean(PREF_KEY_ENABLED, DEFAULT_ENABLED);
    }

    public static void ensureDefaults(SharedPreferences preferences) {
        if (preferences != null && !preferences.contains(PREF_KEY_ENABLED)) {
            preferences.edit().putBoolean(PREF_KEY_ENABLED, DEFAULT_ENABLED).apply();
        }
    }

    public static boolean isEnabled(Context context) {
        Boolean cached = cachedEnabled;
        if (cached != null) {
            return cached.booleanValue();
        }
        boolean enabled = readEnabled(preferences(context));
        cachedEnabled = Boolean.valueOf(enabled);
        return enabled;
    }

    public static boolean writeEnabled(SharedPreferences preferences, boolean enabled) {
        if (preferences == null) {
            return false;
        }
        invalidateCache();
        return preferences.edit().putBoolean(PREF_KEY_ENABLED, enabled).commit();
    }

    public static void invalidateCache() {
        cachedEnabled = null;
    }
}
