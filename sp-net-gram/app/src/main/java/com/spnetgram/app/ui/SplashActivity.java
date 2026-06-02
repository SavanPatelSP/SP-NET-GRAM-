package com.spnetgram.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.security.SecureStorageManager;
import com.spnetgram.app.ui.auth.PhoneAuthActivity;

public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_splash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean done = SPNetGramApp.getInstance().getSecureStorage()
                .getBoolean(SecureStorageManager.KEY_ONBOARDING_DONE, false);
            startActivity(new Intent(this, done ? MainActivity.class : PhoneAuthActivity.class));
            finish();
        }, 1800);
    }
}
