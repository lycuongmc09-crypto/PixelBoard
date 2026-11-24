/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.glass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakHashMap;
import java.util.Map;

/**
 * Phase 1 of the PixelBoard liquid-glass experiment.
 *
 * <p>Installs a lightweight frosted overlay on top of the container that hosts
 * Gboard's Rambler voice toolbar. This tier needs no backdrop provider and no
 * AGSL shader: it draws a translucent gradient panel with a specular rim, so
 * it is safe on every API level and costs almost nothing per frame.</p>
 *
 * <p>The view is added asynchronously to avoid mutating the view hierarchy
 * while Gboard is drawing it, and installation is idempotent.</p>
 */
public final class GboardGlassOverlayRuntime {
    private static final String TAG = "PixelBoardGlass";
    private static final String OVERLAY_TAG = "pixelboard_glass_overlay";
    private static final float OVERLAY_HEIGHT_DP = 56f;
    private static final float CORNER_RADIUS_DP = 18f;

    private static final Object LOCK = new Object();
    private static final WeakHashMap<ViewGroup, GlassOverlayView> OVERLAYS =
            new WeakHashMap<ViewGroup, GlassOverlayView>();

    private GboardGlassOverlayRuntime() {
    }

    /** Entrypoint called from the patched Rambler header method. */
    public static void install(ViewGroup root) {
        if (root == null) {
            return;
        }
        final ViewGroup target = root;
        if (!GboardGlassSettings.isEnabled(target.getContext())) {
            remove(target);
            return;
        }
        target.post(new Runnable() {
            @Override
            public void run() {
                try {
                    installNow(target);
                } catch (Throwable failure) {
                    Log.w(TAG, "Could not install glass overlay", failure);
                }
            }
        });
    }

    private static void installNow(ViewGroup root) {
        synchronized (LOCK) {
            if (findOverlay(root) != null) {
                return;
            }
            GlassOverlayView overlay = new GlassOverlayView(root.getContext());
            overlay.setTag(OVERLAY_TAG);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(root.getContext(), OVERLAY_HEIGHT_DP));
            root.addView(overlay, params);
            OVERLAYS.put(root, overlay);
        }
    }

    private static View findOverlay(ViewGroup root) {
        if (root == null) {
            return null;
        }
        View tagged = root.findViewWithTag(OVERLAY_TAG);
        if (tagged != null) {
            return tagged;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof GlassOverlayView) {
                return child;
            }
        }
        return null;
    }

    private static void remove(ViewGroup root) {
        synchronized (LOCK) {
            OVERLAYS.remove(root);
            View overlay = findOverlay(root);
            if (overlay != null && overlay.getParent() instanceof ViewGroup) {
                ((ViewGroup) overlay.getParent()).removeView(overlay);
            }
        }
    }

    /** Removes every overlay, used when the user turns the preference off. */
    public static void removeAll() {
        synchronized (LOCK) {
            for (Map.Entry<ViewGroup, GlassOverlayView> entry : OVERLAYS.entrySet()) {
                View overlay = entry.getValue();
                if (overlay.getParent() instanceof ViewGroup) {
                    ((ViewGroup) overlay.getParent()).removeView(overlay);
                }
            }
            OVERLAYS.clear();
        }
    }

    private static float dp(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    /** Static frosted panel with a rim highlight. No provider, no shader. */
    static final class GlassOverlayView extends View {
        private final RectF bounds = new RectF();
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float cornerRadius;

        GlassOverlayView(Context context) {
            super(context);
            setWillNotDraw(false);
            setClickable(false);
            setFocusable(false);
            cornerRadius = dp(context, CORNER_RADIUS_DP);
            rim.setStyle(Paint.Style.STROKE);
            rim.setColor(0x66FFFFFF);
            rim.setStrokeWidth(dp(context, 1f));
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            fill.setShader(new LinearGradient(
                    0f, 0f, 0f, Math.max(1, height),
                    0x3DFFFFFF, 0x0AFFFFFF, Shader.TileMode.CLAMP));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getWidth() < 2 || getHeight() < 2) {
                return;
            }
            bounds.set(0f, 0f, getWidth(), getHeight());
            canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, fill);
            canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, rim);
        }
    }
}
