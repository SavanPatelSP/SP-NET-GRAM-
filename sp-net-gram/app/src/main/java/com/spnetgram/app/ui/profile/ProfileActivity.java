package com.spnetgram.app.ui.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;

public class ProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_profile);
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvBadge = findViewById(R.id.tv_diamond_badge);
        String badge = SPNetGramApp.getInstance().getDiamondManager().getBadgeLabel();
        tvBadge.setVisibility(badge.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        tvBadge.setText(badge);
    }
    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
