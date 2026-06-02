package com.spnetgram.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.spnetgram.app.analytics.AnalyticsManager;
import com.spnetgram.app.premium.SPCoinsManager;
import com.spnetgram.app.premium.SPDiamondManager;
import com.spnetgram.app.security.SecureStorageManager;
import com.spnetgram.app.theme.ThemeEngine;
import com.spnetgram.app.utils.AccountManager;
import com.spnetgram.app.utils.NotificationHelper;

public class SPNetGramApp extends Application {

    private static final String TAG = "SPNetGramApp";
    private static SPNetGramApp instance;

    private ThemeEngine themeEngine;
    private AccountManager accountManager;
    private SPDiamondManager diamondManager;
    private SPCoinsManager coinsManager;
    private AnalyticsManager analyticsManager;
    private SecureStorageManager secureStorage;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }

        initFirebase();
        initCoreManagers();
        initThemeEngine();
        initNotifications();

        Log.i(TAG, "SP NET GRAM v" + BuildConfig.APP_VERSION + " initialized");
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);

        // Disable Crashlytics in debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);

        // Analytics only if user consented
        analyticsManager = new AnalyticsManager(this);
        boolean analyticsConsented = secureStorage != null &&
            secureStorage.getBoolean(SecureStorageManager.KEY_ANALYTICS_CONSENT, false);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(
            BuildConfig.ANALYTICS_ENABLED && analyticsConsented
        );
    }

    private void initCoreManagers() {
        secureStorage = SecureStorageManager.getInstance(this);
        accountManager = AccountManager.getInstance(this);
        diamondManager = SPDiamondManager.getInstance(this);
        coinsManager = SPCoinsManager.getInstance(this);
        analyticsManager = AnalyticsManager.getInstance(this);
    }

    private void initThemeEngine() {
        themeEngine = ThemeEngine.getInstance(this);
        themeEngine.applyCurrentTheme();
    }

    private void initNotifications() {
        NotificationHelper.createNotificationChannels(this);
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyLog()
            .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .penaltyLog()
            .build());
    }

    public static SPNetGramApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public ThemeEngine getThemeEngine() { return themeEngine; }
    public AccountManager getAccountManager() { return accountManager; }
    public SPDiamondManager getDiamondManager() { return diamondManager; }
    public SPCoinsManager getCoinsManager() { return coinsManager; }
    public AnalyticsManager getAnalyticsManager() { return analyticsManager; }
    public SecureStorageManager getSecureStorage() { return secureStorage; }
}
