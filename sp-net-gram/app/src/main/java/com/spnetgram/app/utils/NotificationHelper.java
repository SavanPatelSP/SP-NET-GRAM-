package com.spnetgram.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

public class NotificationHelper {

    public static final String CHANNEL_MESSAGES  = "sp_messages";
    public static final String CHANNEL_CALLS     = "sp_calls";
    public static final String CHANNEL_SYSTEM    = "sp_system";
    public static final String CHANNEL_COINS     = "sp_coins";
    public static final String CHANNEL_SILENT    = "sp_silent";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;

        // Messages channel (high priority, sound)
        NotificationChannel messages = new NotificationChannel(
            CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH);
        messages.setDescription("New messages and chats");
        messages.enableVibration(true);
        messages.setVibrationPattern(new long[]{0, 250, 250, 250});
        messages.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        );
        nm.createNotificationChannel(messages);

        // Calls channel (max priority)
        NotificationChannel calls = new NotificationChannel(
            CHANNEL_CALLS, "Calls", NotificationManager.IMPORTANCE_MAX);
        calls.setDescription("Incoming voice and video calls");
        calls.enableVibration(true);
        nm.createNotificationChannel(calls);

        // System channel (default priority)
        NotificationChannel system = new NotificationChannel(
            CHANNEL_SYSTEM, "System", NotificationManager.IMPORTANCE_DEFAULT);
        system.setDescription("System announcements and updates");
        nm.createNotificationChannel(system);

        // Coins/rewards channel (low priority)
        NotificationChannel coins = new NotificationChannel(
            CHANNEL_COINS, "SP Coins & Rewards", NotificationManager.IMPORTANCE_LOW);
        coins.setDescription("SP Coins earned and referral rewards");
        nm.createNotificationChannel(coins);

        // Silent channel (no sound, no vibration)
        NotificationChannel silent = new NotificationChannel(
            CHANNEL_SILENT, "Silent", NotificationManager.IMPORTANCE_LOW);
        silent.setDescription("Silent notifications");
        silent.setSound(null, null);
        silent.enableVibration(false);
        nm.createNotificationChannel(silent);
    }
}
