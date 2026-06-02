package com.spnetgram.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.security.SecureStorageManager;
import com.spnetgram.app.ui.GDPRConsentActivity;
import com.spnetgram.app.ui.MainActivity;

/** Two-step verification / cloud password screen. */
public class PasswordActivity extends AppCompatActivity {
    private EditText etPassword;
    private Button btnSubmit;
    private ProgressBar progressBar;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_password);
        etPassword  = findViewById(R.id.et_password);
        btnSubmit   = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);

        btnSubmit.setOnClickListener(v -> {
            String pwd = etPassword.getText().toString();
            if (pwd.isEmpty()) { etPassword.setError("Enter your password"); return; }
            setLoading(true);
            // TelegramConnectionManager.checkPassword(pwd, () -> onAuthSuccess(), err -> { setLoading(false); etPassword.setError(err); });
            onAuthSuccess(); // STUB
        });
    }

    private void onAuthSuccess() {
        SPNetGramApp.getInstance().getSecureStorage().putBoolean(SecureStorageManager.KEY_ONBOARDING_DONE, true);
        boolean gdpr = SPNetGramApp.getInstance().getSecureStorage().getBoolean(SecureStorageManager.KEY_GDPR_ACCEPTED, false);
        startActivity(new Intent(this, gdpr ? MainActivity.class : GDPRConsentActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void setLoading(boolean l) {
        progressBar.setVisibility(l ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!l);
    }
}
