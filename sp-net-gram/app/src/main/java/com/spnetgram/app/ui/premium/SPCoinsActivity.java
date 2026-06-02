package com.spnetgram.app.ui.premium;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.premium.SPCoinsManager;

public class SPCoinsActivity extends AppCompatActivity
        implements SPCoinsManager.CoinsBalanceListener {

    private TextView tvBalance;
    private Button btnClaimDaily;
    private Button btnShareReferral;
    private Button btnEnterCode;
    private EditText etReferralCode;
    private SPCoinsManager coinsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp_coins);

        coinsManager = SPNetGramApp.getInstance().getCoinsManager();
        coinsManager.addListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("SP Coins");
        }

        tvBalance       = findViewById(R.id.tv_balance);
        btnClaimDaily   = findViewById(R.id.btn_claim_daily);
        btnShareReferral = findViewById(R.id.btn_share_referral);
        btnEnterCode    = findViewById(R.id.btn_enter_referral_code);
        etReferralCode  = findViewById(R.id.et_referral_code);

        updateBalance(coinsManager.getBalance());
        refreshFromServer();
        setupButtons();
    }

    private void setupButtons() {
        btnClaimDaily.setOnClickListener(v -> {
            btnClaimDaily.setEnabled(false);
            coinsManager.dailyLoginReward(new SPCoinsManager.CoinsCallback() {
                @Override public void onSuccess(long newBalance) {
                    runOnUiThread(() -> {
                        btnClaimDaily.setEnabled(true);
                        Toast.makeText(SPCoinsActivity.this,
                            "+" + SPCoinsManager.REWARD_DAILY_LOGIN + " SP Coins claimed!", Toast.LENGTH_SHORT).show();
                        SPNetGramApp.getInstance().getAnalyticsManager()
                            .logCoinsEarned(SPCoinsManager.REWARD_DAILY_LOGIN, "daily_login");
                    });
                }
                @Override public void onError(String error) {
                    runOnUiThread(() -> {
                        btnClaimDaily.setEnabled(true);
                        Toast.makeText(SPCoinsActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        btnShareReferral.setOnClickListener(v ->
            coinsManager.generateReferralCode(new SPCoinsManager.ReferralCodeCallback() {
                @Override public void onCode(String code) {
                    runOnUiThread(() -> {
                        android.content.Intent shareIntent = new android.content.Intent(
                            android.content.Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                            "Join SP NET GRAM and get bonus coins! Use my referral code: " + code
                            + "\nDownload: https://spnetgram.com");
                        startActivity(android.content.Intent.createChooser(shareIntent, "Share SP NET GRAM"));
                    });
                }
                @Override public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(SPCoinsActivity.this, error, Toast.LENGTH_SHORT).show());
                }
            }));

        btnEnterCode.setOnClickListener(v -> {
            String code = etReferralCode.getText().toString().trim();
            if (code.isEmpty()) { etReferralCode.setError("Enter a code"); return; }
            btnEnterCode.setEnabled(false);
            coinsManager.referralReward(code, new SPCoinsManager.CoinsCallback() {
                @Override public void onSuccess(long newBalance) {
                    runOnUiThread(() -> {
                        btnEnterCode.setEnabled(true);
                        etReferralCode.setText("");
                        Toast.makeText(SPCoinsActivity.this,
                            "+" + SPCoinsManager.REWARD_REFERRAL + " SP Coins! Thanks for using a referral code.", Toast.LENGTH_LONG).show();
                    });
                }
                @Override public void onError(String error) {
                    runOnUiThread(() -> {
                        btnEnterCode.setEnabled(true);
                        Toast.makeText(SPCoinsActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void refreshFromServer() {
        coinsManager.fetchBalance(this::updateBalance);
    }

    private void updateBalance(long balance) {
        runOnUiThread(() -> tvBalance.setText(balance + " SP Coins"));
    }

    @Override public void onBalanceChanged(long newBalance) { updateBalance(newBalance); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        coinsManager.removeListener(this);
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
