package com.spnetgram.app.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Encrypted key-value storage for sensitive data.
 * Uses AndroidX Security EncryptedSharedPreferences backed by AES256-GCM + AES256-SIV.
 */
public class SecureStorageManager {

    private static final String TAG = "SecureStorageManager";
    private static final String PREF_FILE = "spnetgram_secure_prefs";

    // Storage keys
    public static final String KEY_OPENAI_API_KEY       = "openai_api_key";
    public static final String KEY_ANALYTICS_CONSENT    = "analytics_consent";
    public static final String KEY_DIAMOND_TIER         = "diamond_tier";
    public static final String KEY_COINS_BALANCE        = "coins_balance";
    public static final String KEY_LAST_DAILY_REWARD    = "last_daily_reward";
    public static final String KEY_REFERRAL_CODE        = "referral_code";
    public static final String KEY_THEME_CONFIG         = "theme_config";
    public static final String KEY_ACTIVE_ACCOUNT_ID    = "active_account_id";
    public static final String KEY_PASSCODE_HASH        = "passcode_hash";
    public static final String KEY_BIOMETRIC_ENABLED    = "biometric_enabled";
    public static final String KEY_GDPR_ACCEPTED        = "gdpr_accepted";
    public static final String KEY_GDPR_VERSION         = "gdpr_version";
    public static final String KEY_ONBOARDING_DONE      = "onboarding_done";
    public static final String KEY_NOTIFICATION_TOKEN   = "notification_token";
    public static final String KEY_APP_LOCK_ENABLED     = "app_lock_enabled";

    private static volatile SecureStorageManager instance;
    private SharedPreferences prefs;

    private SecureStorageManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

            prefs = EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to create encrypted prefs, falling back to plain prefs", e);
            prefs = context.getSharedPreferences(PREF_FILE + "_plain", Context.MODE_PRIVATE);
        }
    }

    public static SecureStorageManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SecureStorageManager.class) {
                if (instance == null) {
                    instance = new SecureStorageManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
