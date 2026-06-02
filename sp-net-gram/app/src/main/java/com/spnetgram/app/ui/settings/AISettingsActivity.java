package com.spnetgram.app.ui.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.security.SecureStorageManager;

public class AISettingsActivity extends AppCompatActivity {

    private EditText etApiKey;
    private Button btnSave;
    private Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("AI Features");
        }

        etApiKey = findViewById(R.id.et_openai_api_key);
        btnSave  = findViewById(R.id.btn_save_api_key);
        btnTest  = findViewById(R.id.btn_test_api_key);

        // Show masked existing key
        String existing = SPNetGramApp.getInstance().getSecureStorage()
            .getString(SecureStorageManager.KEY_OPENAI_API_KEY, "");
        if (existing != null && !existing.isEmpty()) {
            etApiKey.setHint("sk-****" + existing.substring(Math.max(0, existing.length() - 4)));
        }

        btnSave.setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            if (key.isEmpty()) { Toast.makeText(this, "Enter your API key", Toast.LENGTH_SHORT).show(); return; }
            if (!key.startsWith("sk-")) { Toast.makeText(this, "API key should start with 'sk-'", Toast.LENGTH_SHORT).show(); return; }

            SPNetGramApp.getInstance().getSecureStorage()
                .putString(SecureStorageManager.KEY_OPENAI_API_KEY, key);
            Toast.makeText(this, "API key saved", Toast.LENGTH_SHORT).show();
            etApiKey.setText("");
        });

        btnTest.setOnClickListener(v -> {
            String key = SPNetGramApp.getInstance().getSecureStorage()
                .getString(SecureStorageManager.KEY_OPENAI_API_KEY, "");
            if (key == null || key.isEmpty()) {
                Toast.makeText(this, "No API key saved", Toast.LENGTH_SHORT).show();
                return;
            }
            btnTest.setEnabled(false);
            btnTest.setText("Testing…");
            com.spnetgram.app.ai.AIManager.getInstance(this).summarizeMessages(
                "Hello this is a test message", "English",
                new com.spnetgram.app.ai.AIManager.AICallback() {
                    @Override public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            btnTest.setEnabled(true);
                            btnTest.setText("Test API Key");
                            Toast.makeText(AISettingsActivity.this, "✓ API key works!", Toast.LENGTH_LONG).show();
                        });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> {
                            btnTest.setEnabled(true);
                            btnTest.setText("Test API Key");
                            Toast.makeText(AISettingsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
        });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
