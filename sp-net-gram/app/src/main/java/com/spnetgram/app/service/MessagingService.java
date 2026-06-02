package com.spnetgram.app.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.spnetgram.app.R;
import com.spnetgram.app.utils.NotificationHelper;

/**
 * Persistent background service that maintains the Telegram MTProto connection.
 * Runs as a foreground service to prevent the OS from killing it.
 */
public class MessagingService extends Service {
    private static final String TAG = "MessagingService";
    private static final int NOTIF_ID = 1;

    @Override public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MessagingService started");
        startForeground(NOTIF_ID, buildForegroundNotification());
        // Initialize Telegram MTProto connection here
        // TelegramConnectionManager.getInstance(this).connect();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MessagingService stopped");
        // TelegramConnectionManager.getInstance(this).disconnect();
    }

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_SILENT)
            .setContentTitle("SP NET GRAM")
            .setContentText("Connected")
            .setSmallIcon(R.drawable.ic_notification)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build();
    }
}
