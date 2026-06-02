package com.spnetgram.app.analytics;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.spnetgram.app.security.SecureStorageManager;

/**
 * SP NET GRAM Analytics Manager.
 *
 * PRIVACY POLICY:
 * - Only collects events if the user has explicitly consented (GDPR-style).
 * - NEVER logs message content, contact names, phone numbers, or any PII.
 * - Only collects: active sessions, device category, app version, feature usage flags.
 */
public class AnalyticsManager {

    private static final String TAG = "AnalyticsManager";

    // Event names — only non-PII signals
    public static final String EVENT_APP_OPEN            = "app_open";
    public static final String EVENT_SESSION_START       = "session_start";
    public static final String EVENT_AI_FEATURE_USED     = "ai_feature_used";
    public static final String EVENT_DIAMOND_PURCHASED   = "diamond_purchased";
    public static final String EVENT_COINS_EARNED        = "coins_earned";
    public static final String EVENT_COINS_SPENT         = "coins_spent";
    public static final String EVENT_THEME_CHANGED       = "theme_changed";
    public static final String EVENT_ACCOUNT_ADDED       = "account_added";
    public static final String EVENT_FEATURE_FLAG_SEEN   = "feature_flag_seen";
    public static final String EVENT_SETTINGS_OPENED     = "settings_opened";

    // Parameter keys
    public static final String PARAM_FEATURE_NAME   = "feature_name";
    public static final String PARAM_TIER           = "tier";
    public static final String PARAM_AMOUNT         = "amount";
    public static final String PARAM_THEME_NAME     = "theme_name";
    public static final String PARAM_APP_VERSION    = "app_version";

    private static volatile AnalyticsManager instance;

    private final Context context;
    private final SecureStorageManager secureStorage;
    private FirebaseAnalytics firebaseAnalytics;
    private boolean consented = false;

    private AnalyticsManager(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = SecureStorageManager.getInstance(context);
        this.consented = secureStorage.getBoolean(SecureStorageManager.KEY_ANALYTICS_CONSENT, false);
        if (consented) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
    }

    public static AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AnalyticsManager.class) {
                if (instance == null) {
                    instance = new AnalyticsManager(context);
                }
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Consent
    // ──────────────────────────────────────────────────────────────────────────

    public void setConsent(boolean consented) {
        this.consented = consented;
        secureStorage.putBoolean(SecureStorageManager.KEY_ANALYTICS_CONSENT, consented);

        if (consented) {
            if (firebaseAnalytics == null) {
                firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            }
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        } else {
            if (firebaseAnalytics != null) {
                firebaseAnalytics.setAnalyticsCollectionEnabled(false);
            }
        }
        Log.i(TAG, "Analytics consent set to: " + consented);
    }

    public boolean isConsented() { return consented; }

    // ──────────────────────────────────────────────────────────────────────────
    // Event logging
    // ──────────────────────────────────────────────────────────────────────────

    public void logEvent(String eventName, Bundle params) {
        if (!consented || firebaseAnalytics == null) return;
        // Final safety check: never log if params contain any PII-like keys
        if (params != null && containsPii(params)) {
            Log.w(TAG, "Blocked analytics event with potential PII: " + eventName);
            return;
        }
        firebaseAnalytics.logEvent(eventName, params);
    }

    public void logEvent(String eventName) {
        logEvent(eventName, null);
    }

    public void logAppOpen() {
        Bundle b = new Bundle();
        b.putString(PARAM_APP_VERSION, com.spnetgram.app.BuildConfig.APP_VERSION);
        logEvent(EVENT_APP_OPEN, b);
    }

    public void logAIFeatureUsed(String featureName) {
        Bundle b = new Bundle();
        b.putString(PARAM_FEATURE_NAME, featureName);
        logEvent(EVENT_AI_FEATURE_USED, b);
    }

    public void logDiamondPurchased(String tier) {
        Bundle b = new Bundle();
        b.putString(PARAM_TIER, tier);
        logEvent(EVENT_DIAMOND_PURCHASED, b);
    }

    public void logCoinsEarned(int amount, String reason) {
        Bundle b = new Bundle();
        b.putInt(PARAM_AMOUNT, amount);
        b.putString(PARAM_FEATURE_NAME, reason);
        logEvent(EVENT_COINS_EARNED, b);
    }

    public void logCoinsSpent(int amount, String reason) {
        Bundle b = new Bundle();
        b.putInt(PARAM_AMOUNT, amount);
        b.putString(PARAM_FEATURE_NAME, reason);
        logEvent(EVENT_COINS_SPENT, b);
    }

    public void logThemeChanged(String themeName) {
        Bundle b = new Bundle();
        b.putString(PARAM_THEME_NAME, themeName);
        logEvent(EVENT_THEME_CHANGED, b);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PII guard
    // ──────────────────────────────────────────────────────────────────────────

    private boolean containsPii(Bundle bundle) {
        for (String key : bundle.keySet()) {
            String lk = key.toLowerCase();
            if (lk.contains("phone") || lk.contains("email") || lk.contains("name")
                    || lk.contains("message") || lk.contains("content")
                    || lk.contains("text") || lk.contains("uid") || lk.contains("id")) {
                return true;
            }
        }
        return false;
    }
}
