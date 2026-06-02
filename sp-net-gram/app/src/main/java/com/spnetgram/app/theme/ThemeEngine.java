package com.spnetgram.app.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.spnetgram.app.security.SecureStorageManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced theme engine for SP NET GRAM.
 * Supports light/dark modes, glassmorphism, custom accent colors,
 * chat bubble styles, and background wallpapers.
 */
public class ThemeEngine {

    private static final String TAG = "ThemeEngine";

    public enum Mode { LIGHT, DARK, SYSTEM }

    public enum BubbleStyle { CLASSIC, MODERN, MINIMAL, ROUNDED_HEAVY, FLAT }

    public enum BackgroundType { SOLID, GRADIENT, WALLPAPER, ANIMATED }

    /** Built-in preset themes */
    public enum ThemePreset {
        DEFAULT_BLUE, MIDNIGHT, ROSE_GOLD, FOREST_GREEN,
        SUNSET_ORANGE, ARCTIC, PURPLE_HAZE, CUSTOM
    }

    private static volatile ThemeEngine instance;

    private final Context context;
    private final SecureStorageManager secureStorage;
    private ThemeConfig currentConfig;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    private ThemeEngine(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = SecureStorageManager.getInstance(context);
        this.currentConfig = loadConfig();
    }

    public static ThemeEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (ThemeEngine.class) {
                if (instance == null) {
                    instance = new ThemeEngine(context);
                }
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Apply
    // ──────────────────────────────────────────────────────────────────────────

    public void applyCurrentTheme() {
        switch (currentConfig.mode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public void setMode(Mode mode) {
        currentConfig.mode = mode;
        saveConfig();
        applyCurrentTheme();
        notifyListeners();
    }

    public void setPreset(ThemePreset preset) {
        currentConfig = ThemeConfig.fromPreset(preset);
        saveConfig();
        notifyListeners();
    }

    public void setCustomAccentColor(@ColorInt int color) {
        currentConfig.preset = ThemePreset.CUSTOM;
        currentConfig.accentColor = color;
        saveConfig();
        notifyListeners();
    }

    public void setGlassmorphismEnabled(boolean enabled) {
        currentConfig.glassmorphismEnabled = enabled;
        saveConfig();
        notifyListeners();
    }

    public void setBubbleStyle(BubbleStyle style) {
        currentConfig.bubbleStyle = style;
        saveConfig();
        notifyListeners();
    }

    public void setChatBackground(BackgroundType type, String value) {
        currentConfig.backgroundType = type;
        currentConfig.backgroundValue = value;
        saveConfig();
        notifyListeners();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Getters
    // ──────────────────────────────────────────────────────────────────────────

    public ThemeConfig getConfig() { return currentConfig; }
    public Mode getMode() { return currentConfig.mode; }
    public boolean isGlassmorphismEnabled() { return currentConfig.glassmorphismEnabled; }
    public BubbleStyle getBubbleStyle() { return currentConfig.bubbleStyle; }
    public @ColorInt int getAccentColor() { return currentConfig.accentColor; }
    public @ColorInt int getPrimaryColor() { return currentConfig.primaryColor; }
    public @ColorInt int getBackgroundColor() { return currentConfig.backgroundColor; }
    public @ColorInt int getSurfaceColor() { return currentConfig.surfaceColor; }

    public boolean isDarkMode() {
        return currentConfig.mode == Mode.DARK;
    }

    public ColorStateList getAccentColorStateList() {
        return ColorStateList.valueOf(currentConfig.accentColor);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Glassmorphism helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns the blur radius for glassmorphism effects (dp).
     * Adjust based on the surface's purpose — heavier blur for modals.
     */
    public int getBlurRadius(GlassLayer layer) {
        if (!currentConfig.glassmorphismEnabled) return 0;
        switch (layer) {
            case CHAT_HEADER:   return 20;
            case BOTTOM_BAR:    return 25;
            case MODAL:         return 35;
            case NOTIFICATION:  return 15;
            case BUBBLE_BG:     return 10;
            default:            return 20;
        }
    }

    public float getGlassAlpha(GlassLayer layer) {
        if (!currentConfig.glassmorphismEnabled) return 1.0f;
        switch (layer) {
            case CHAT_HEADER:   return 0.75f;
            case BOTTOM_BAR:    return 0.85f;
            case MODAL:         return 0.80f;
            case NOTIFICATION:  return 0.70f;
            case BUBBLE_BG:     return 0.60f;
            default:            return 0.75f;
        }
    }

    public int getGlassTintColor(GlassLayer layer) {
        boolean dark = isDarkMode();
        switch (layer) {
            case BUBBLE_BG:
                return dark ? Color.argb(40, 255, 255, 255) : Color.argb(60, 255, 255, 255);
            default:
                return dark ? Color.argb(50, 30, 30, 30) : Color.argb(60, 255, 255, 255);
        }
    }

    public enum GlassLayer {
        CHAT_HEADER, BOTTOM_BAR, MODAL, NOTIFICATION, BUBBLE_BG
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Persistence
    // ──────────────────────────────────────────────────────────────────────────

    private void saveConfig() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", currentConfig.mode.name());
            json.put("preset", currentConfig.preset.name());
            json.put("accentColor", currentConfig.accentColor);
            json.put("primaryColor", currentConfig.primaryColor);
            json.put("backgroundColor", currentConfig.backgroundColor);
            json.put("surfaceColor", currentConfig.surfaceColor);
            json.put("glassmorphism", currentConfig.glassmorphismEnabled);
            json.put("bubbleStyle", currentConfig.bubbleStyle.name());
            json.put("backgroundType", currentConfig.backgroundType.name());
            json.put("backgroundValue", currentConfig.backgroundValue != null ? currentConfig.backgroundValue : "");
            secureStorage.putString(SecureStorageManager.KEY_THEME_CONFIG, json.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to save theme config", e);
        }
    }

    private ThemeConfig loadConfig() {
        String json = secureStorage.getString(SecureStorageManager.KEY_THEME_CONFIG, null);
        if (json == null) return ThemeConfig.fromPreset(ThemePreset.DEFAULT_BLUE);
        try {
            JSONObject obj = new JSONObject(json);
            ThemeConfig cfg = new ThemeConfig();
            cfg.mode = Mode.valueOf(obj.optString("mode", Mode.SYSTEM.name()));
            cfg.preset = ThemePreset.valueOf(obj.optString("preset", ThemePreset.DEFAULT_BLUE.name()));
            cfg.accentColor = obj.optInt("accentColor", 0xFF2196F3);
            cfg.primaryColor = obj.optInt("primaryColor", 0xFF1565C0);
            cfg.backgroundColor = obj.optInt("backgroundColor", 0xFFFFFFFF);
            cfg.surfaceColor = obj.optInt("surfaceColor", 0xFFF5F5F5);
            cfg.glassmorphismEnabled = obj.optBoolean("glassmorphism", false);
            cfg.bubbleStyle = BubbleStyle.valueOf(obj.optString("bubbleStyle", BubbleStyle.MODERN.name()));
            cfg.backgroundType = BackgroundType.valueOf(obj.optString("backgroundType", BackgroundType.SOLID.name()));
            cfg.backgroundValue = obj.optString("backgroundValue", "");
            return cfg;
        } catch (Exception e) {
            Log.w(TAG, "Could not parse theme config, using default", e);
            return ThemeConfig.fromPreset(ThemePreset.DEFAULT_BLUE);
        }
    }

    private void notifyListeners() {
        for (ThemeChangeListener l : listeners) l.onThemeChanged(currentConfig);
    }

    public void addListener(ThemeChangeListener l) { listeners.add(l); }
    public void removeListener(ThemeChangeListener l) { listeners.remove(l); }

    public interface ThemeChangeListener {
        void onThemeChanged(ThemeConfig config);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ThemeConfig
    // ──────────────────────────────────────────────────────────────────────────

    public static class ThemeConfig {
        public Mode mode = Mode.SYSTEM;
        public ThemePreset preset = ThemePreset.DEFAULT_BLUE;
        public @ColorInt int accentColor    = 0xFF2196F3;
        public @ColorInt int primaryColor   = 0xFF1565C0;
        public @ColorInt int backgroundColor = 0xFFFFFFFF;
        public @ColorInt int surfaceColor   = 0xFFF5F5F5;
        public boolean glassmorphismEnabled = false;
        public BubbleStyle bubbleStyle = BubbleStyle.MODERN;
        public BackgroundType backgroundType = BackgroundType.SOLID;
        public String backgroundValue = "";

        public static ThemeConfig fromPreset(@NonNull ThemePreset preset) {
            ThemeConfig cfg = new ThemeConfig();
            cfg.preset = preset;
            switch (preset) {
                case MIDNIGHT:
                    cfg.mode = Mode.DARK;
                    cfg.accentColor    = 0xFF7C4DFF;
                    cfg.primaryColor   = 0xFF311B92;
                    cfg.backgroundColor = 0xFF121212;
                    cfg.surfaceColor   = 0xFF1E1E1E;
                    break;
                case ROSE_GOLD:
                    cfg.accentColor    = 0xFFB76E79;
                    cfg.primaryColor   = 0xFF8B4A52;
                    cfg.backgroundColor = 0xFFFFF8F8;
                    cfg.surfaceColor   = 0xFFFCF0F0;
                    break;
                case FOREST_GREEN:
                    cfg.accentColor    = 0xFF388E3C;
                    cfg.primaryColor   = 0xFF1B5E20;
                    cfg.backgroundColor = 0xFFF1F8E9;
                    cfg.surfaceColor   = 0xFFE8F5E9;
                    break;
                case SUNSET_ORANGE:
                    cfg.accentColor    = 0xFFFF6D00;
                    cfg.primaryColor   = 0xFFE65100;
                    cfg.backgroundColor = 0xFFFFF8F3;
                    cfg.surfaceColor   = 0xFFFFF3E0;
                    break;
                case ARCTIC:
                    cfg.mode = Mode.LIGHT;
                    cfg.accentColor    = 0xFF00ACC1;
                    cfg.primaryColor   = 0xFF006064;
                    cfg.backgroundColor = 0xFFEEF9FF;
                    cfg.surfaceColor   = 0xFFE0F7FA;
                    break;
                case PURPLE_HAZE:
                    cfg.accentColor    = 0xFF9C27B0;
                    cfg.primaryColor   = 0xFF6A1B9A;
                    cfg.backgroundColor = 0xFFF8F0FF;
                    cfg.surfaceColor   = 0xFFF3E5F5;
                    break;
                default: // DEFAULT_BLUE
                    cfg.accentColor    = 0xFF2196F3;
                    cfg.primaryColor   = 0xFF1565C0;
                    cfg.backgroundColor = 0xFFFFFFFF;
                    cfg.surfaceColor   = 0xFFF5F5F5;
                    break;
            }
            return cfg;
        }
    }
}
