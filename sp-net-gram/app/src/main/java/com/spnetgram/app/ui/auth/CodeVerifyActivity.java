package com.spnetgram.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

public class CodeVerifyActivity extends AppCompatActivity {

    private EditText etCode;
    private Button btnVerify;
    private Button btnResend;
    private ProgressBar progressBar;
    private TextView tvPhone;
    private TextView tvCountdown;

    private String phone;
    private String phoneCodeHash;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verify);

        phone         = getIntent().getStringExtra("phone");
        phoneCodeHash = getIntent().getStringExtra("phone_code_hash");

        etCode      = findViewById(R.id.et_code);
        btnVerify   = findViewById(R.id.btn_verify);
        btnResend   = findViewById(R.id.btn_resend);
        progressBar = findViewById(R.id.progress_bar);
        tvPhone     = findViewById(R.id.tv_phone);
        tvCountdown = findViewById(R.id.tv_countdown);

        tvPhone.setText("Code sent to " + phone);
        startResendTimer();
        setupVerifyButton();
        setupResendButton();
    }

    private void setupVerifyButton() {
        btnVerify.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.length() < 5) { etCode.setError("Enter 5-digit code"); return; }

            setLoading(true);
            // TelegramConnectionManager.signIn(phone, phoneCodeHash, code,
            //     () -> onAuthSuccess(),
            //     error -> { setLoading(false); etCode.setError(error); }
            // );

            // STUB: simulate auth
            onAuthSuccess();
        });
    }

    private void setupResendButton() {
        btnResend.setEnabled(false);
        btnResend.setOnClickListener(v -> {
            btnResend.setEnabled(false);
            startResendTimer();
            // TelegramConnectionManager.resendCode(phone, phoneCodeHash, ...);
        });
    }

    private void startResendTimer() {
        if (resendTimer != null) resendTimer.cancel();
        resendTimer = new CountDownTimer(60000, 1000) {
            @Override public void onTick(long ms) {
                tvCountdown.setText("Resend in " + (ms / 1000) + "s");
            }
            @Override public void onFinish() {
                tvCountdown.setText("");
                btnResend.setEnabled(true);
            }
        }.start();
    }

    private void onAuthSuccess() {
        SPNetGramApp.getInstance().getSecureStorage()
            .putBoolean(SecureStorageManager.KEY_ONBOARDING_DONE, true);

        boolean gdprDone = SPNetGramApp.getInstance().getSecureStorage()
            .getBoolean(SecureStorageManager.KEY_GDPR_ACCEPTED, false);

        Intent next = gdprDone
            ? new Intent(this, MainActivity.class)
            : new Intent(this, GDPRConsentActivity.class);
        next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(next);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) resendTimer.cancel();
    }
}
