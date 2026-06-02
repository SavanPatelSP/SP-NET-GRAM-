package com.spnetgram.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.spnetgram.app.service.MessagingService;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            Log.i("BootReceiver", "Boot completed — starting MessagingService");
            // Restart persistent messaging connection
            Intent svc = new Intent(context, MessagingService.class);
            context.startForegroundService(svc);
        }
    }
}
