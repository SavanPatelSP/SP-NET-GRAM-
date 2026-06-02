package com.spnetgram.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.security.SecureStorageManager;
import com.spnetgram.app.ui.MainActivity;

import java.util.Map;

public class SPFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "SPFirebaseMsg";
    private static final String CHANNEL_MESSAGES = "sp_messages";
    private static final String CHANNEL_SYSTEM   = "sp_system";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Push received from: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        String type = data.getOrDefault("type", "message");

        switch (type) {
            case "message":
                handleMessageNotification(data, remoteMessage.getNotification());
                break;
            case "system":
                handleSystemNotification(data);
                break;
            case "announcement":
                handleAnnouncementNotification(data);
                break;
            case "coins_earned":
                handleCoinsNotification(data);
                break;
            default:
                handleDefaultNotification(remoteMessage);
                break;
        }
    }

    private void handleMessageNotification(Map<String, String> data,
                                            RemoteMessage.Notification notification) {
        // PRIVACY: Never include message content in notification when screen is locked
        String chatId = data.getOrDefault("chat_id", "");
        String senderName = data.getOrDefault("sender_name", "SP NET GRAM");

        // Check if detailed notifications are allowed
        boolean showPreview = SPNetGramApp.getInstance().getSecureStorage()
            .getBoolean("show_notification_preview", true);

        String body = showPreview
            ? data.getOrDefault("preview", "New message")
            : "New message";

        showNotification(chatId.hashCode(), senderName, body, CHANNEL_MESSAGES, chatId);
    }

    private void handleSystemNotification(Map<String, String> data) {
        String title = data.getOrDefault("title", "SP NET GRAM");
        String body = data.getOrDefault("body", "");
        showNotification(9000, title, body, CHANNEL_SYSTEM, null);
    }

    private void handleAnnouncementNotification(Map<String, String> data) {
        String title = data.getOrDefault("title", "Announcement");
        String body = data.getOrDefault("body", "");
        showNotification(9001, title, body, CHANNEL_SYSTEM, null);
    }

    private void handleCoinsNotification(Map<String, String> data) {
        String amount = data.getOrDefault("amount", "");
        String reason = data.getOrDefault("reason", "");
        showNotification(9002, "SP Coins Earned!", "+"+amount+" coins — "+reason, CHANNEL_SYSTEM, null);
    }

    private void handleDefaultNotification(RemoteMessage msg) {
        if (msg.getNotification() != null) {
            showNotification(0, msg.getNotification().getTitle(),
                msg.getNotification().getBody(), CHANNEL_SYSTEM, null);
        }
    }

    private void showNotification(int id, String title, String body, String channelId, String chatId) {
        Intent intent = new Intent(this, MainActivity.class);
        if (chatId != null) intent.putExtra("open_chat_id", chatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(id, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "FCM token refreshed");
        SPNetGramApp.getInstance().getSecureStorage()
            .putString(SecureStorageManager.KEY_NOTIFICATION_TOKEN, token);
        // Send token to your backend
    }
}
