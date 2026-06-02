package com.spnetgram.app.ui.premium;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.premium.SPDiamondManager;

public class SPDiamondActivity extends AppCompatActivity {

    private SPDiamondManager diamondManager;
    private TextView tvCurrentStatus;
    private View cardMonthly, cardYearly;
    private Button btnBuyMonthly, btnBuyYearly;
    private ImageView ivDiamondBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp_diamond);

        diamondManager = SPNetGramApp.getInstance().getDiamondManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("SP Diamond");
        }

        bindViews();
        updateStatusDisplay();
        setupPurchaseButtons();
    }

    private void bindViews() {
        tvCurrentStatus = findViewById(R.id.tv_diamond_status);
        cardMonthly     = findViewById(R.id.card_monthly);
        cardYearly      = findViewById(R.id.card_yearly);
        btnBuyMonthly   = findViewById(R.id.btn_buy_monthly);
        btnBuyYearly    = findViewById(R.id.btn_buy_yearly);
        ivDiamondBadge  = findViewById(R.id.iv_diamond_badge);
    }

    private void updateStatusDisplay() {
        SPDiamondManager.DiamondTier tier = diamondManager.getCurrentTier();
        switch (tier) {
            case MONTHLY:
                tvCurrentStatus.setText("💎 You have SP Diamond (Monthly)");
                cardMonthly.setVisibility(View.GONE);
                break;
            case YEARLY:
                tvCurrentStatus.setText("💎 You have SP Diamond (Yearly)");
                cardMonthly.setVisibility(View.GONE);
                cardYearly.setVisibility(View.GONE);
                break;
            default:
                tvCurrentStatus.setText("Upgrade to unlock all features");
                break;
        }
    }

    private void setupPurchaseButtons() {
        btnBuyMonthly.setOnClickListener(v -> {
            btnBuyMonthly.setEnabled(false);
            btnBuyMonthly.setText("Processing…");
            diamondManager.launchSubscription(this, SPDiamondManager.PRODUCT_MONTHLY,
                new SPDiamondManager.PurchaseCallback() {
                    @Override public void onPurchaseSuccess(SPDiamondManager.DiamondTier tier) {
                        runOnUiThread(() -> {
                            updateStatusDisplay();
                            Toast.makeText(SPDiamondActivity.this,
                                "Welcome to SP Diamond!", Toast.LENGTH_LONG).show();
                        });
                    }
                    @Override public void onError(String message) {
                        runOnUiThread(() -> {
                            btnBuyMonthly.setEnabled(true);
                            btnBuyMonthly.setText("Subscribe Monthly");
                            Toast.makeText(SPDiamondActivity.this, message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
        });

        btnBuyYearly.setOnClickListener(v -> {
            btnBuyYearly.setEnabled(false);
            btnBuyYearly.setText("Processing…");
            diamondManager.launchSubscription(this, SPDiamondManager.PRODUCT_YEARLY,
                new SPDiamondManager.PurchaseCallback() {
                    @Override public void onPurchaseSuccess(SPDiamondManager.DiamondTier tier) {
                        runOnUiThread(() -> {
                            updateStatusDisplay();
                            Toast.makeText(SPDiamondActivity.this,
                                "Welcome to SP Diamond Yearly!", Toast.LENGTH_LONG).show();
                        });
                    }
                    @Override public void onError(String message) {
                        runOnUiThread(() -> {
                            btnBuyYearly.setEnabled(true);
                            btnBuyYearly.setText("Subscribe Yearly (Best Value)");
                            Toast.makeText(SPDiamondActivity.this, message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
