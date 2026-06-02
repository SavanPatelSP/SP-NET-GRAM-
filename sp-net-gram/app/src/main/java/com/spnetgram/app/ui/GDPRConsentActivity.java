package com.spnetgram.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.analytics.AnalyticsManager;
import com.spnetgram.app.security.SecureStorageManager;

public class GDPRConsentActivity extends AppCompatActivity {

    private CheckBox cbAnalytics;
    private Button btnAccept;
    private Button btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdpr_consent);

        cbAnalytics = findViewById(R.id.cb_analytics_consent);
        btnAccept   = findViewById(R.id.btn_gdpr_accept);
        btnDecline  = findViewById(R.id.btn_gdpr_decline);

        btnAccept.setOnClickListener(v -> finishWithConsent(cbAnalytics.isChecked()));
        btnDecline.setOnClickListener(v -> finishWithConsent(false));
    }

    private void finishWithConsent(boolean analyticsConsented) {
        SecureStorageManager storage = SPNetGramApp.getInstance().getSecureStorage();
        storage.putBoolean(SecureStorageManager.KEY_GDPR_ACCEPTED, true);
        storage.putString(SecureStorageManager.KEY_GDPR_VERSION, "1.0");

        SPNetGramApp.getInstance().getAnalyticsManager().setConsent(analyticsConsented);

        startActivity(new Intent(this, MainActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
