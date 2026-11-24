package com.akshaykadam.pixelboard.extension.glass;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class GboardGlassSettingsTest {

    @Test
    public void glassOverlayIsOptInByDefault() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences preferences = GboardGlassSettings.preferences(context);
        preferences.edit().clear().commit();
        GboardGlassSettings.invalidateCache();

        Assert.assertFalse(GboardGlassSettings.readEnabled(preferences));
        Assert.assertFalse(GboardGlassSettings.isEnabled(context));
    }

    @Test
    public void readAndWriteTogglesPreferenceAndInvalidatesCache() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences preferences = GboardGlassSettings.preferences(context);

        Assert.assertTrue(GboardGlassSettings.writeEnabled(preferences, true));
        Assert.assertTrue(GboardGlassSettings.readEnabled(preferences));
        Assert.assertTrue(GboardGlassSettings.isEnabled(context));

        Assert.assertTrue(GboardGlassSettings.writeEnabled(preferences, false));
        Assert.assertFalse(GboardGlassSettings.readEnabled(preferences));
        Assert.assertFalse(GboardGlassSettings.isEnabled(context));
    }
}
