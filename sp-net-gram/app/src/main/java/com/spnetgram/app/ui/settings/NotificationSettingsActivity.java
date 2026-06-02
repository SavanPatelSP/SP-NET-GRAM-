package com.spnetgram.app.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.spnetgram.app.R;

public class NotificationSettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_notification_settings);
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);
        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Notifications"); }
    }
    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
