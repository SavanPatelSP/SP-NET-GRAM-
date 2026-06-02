package com.spnetgram.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.security.SecureStorageManager;

/**
 * Phone number entry screen.
 * Connects to Telegram MTProto to request an SMS/call verification code.
 */
public class PhoneAuthActivity extends AppCompatActivity {

    private EditText etCountryCode;
    private EditText etPhoneNumber;
    private Button btnContinue;
    private ProgressBar progressBar;
    private TextView tvTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        etCountryCode  = findViewById(R.id.et_country_code);
        etPhoneNumber  = findViewById(R.id.et_phone_number);
        btnContinue    = findViewById(R.id.btn_continue);
        progressBar    = findViewById(R.id.progress_bar);
        tvTerms        = findViewById(R.id.tv_terms);

        autoDetectCountryCode();
        setupContinueButton();
    }

    private void autoDetectCountryCode() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (tm != null) {
                String iso = tm.getNetworkCountryIso();
                // Map ISO to dial code
                etCountryCode.setText(isoToDialCode(iso));
            }
        } catch (Exception ignored) {}
    }

    private void setupContinueButton() {
        btnContinue.setOnClickListener(v -> {
            String code  = etCountryCode.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();

            if (phone.isEmpty()) {
                etPhoneNumber.setError("Enter your phone number");
                return;
            }

            String fullNumber = code + phone;
            setLoading(true);

            // Initiate Telegram auth
            // TelegramConnectionManager.requestCode(fullNumber, phoneCodeHash -> {
            //     Intent intent = new Intent(this, CodeVerifyActivity.class);
            //     intent.putExtra("phone", fullNumber);
            //     intent.putExtra("phone_code_hash", phoneCodeHash);
            //     startActivity(intent);
            //     setLoading(false);
            // }, error -> {
            //     setLoading(false);
            //     Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            // });

            // STUB: navigate to code verify
            Intent intent = new Intent(this, CodeVerifyActivity.class);
            intent.putExtra("phone", fullNumber);
            intent.putExtra("phone_code_hash", "stub_hash");
            startActivity(intent);
            setLoading(false);
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnContinue.setEnabled(!loading);
    }

    private String isoToDialCode(String iso) {
        if (iso == null) return "+1";
        switch (iso.toLowerCase()) {
            case "us": case "ca": return "+1";
            case "gb": return "+44";
            case "de": return "+49";
            case "fr": return "+33";
            case "ru": return "+7";
            case "in": return "+91";
            case "cn": return "+86";
            case "jp": return "+81";
            case "br": return "+55";
            case "au": return "+61";
            default: return "+1";
        }
    }
}
