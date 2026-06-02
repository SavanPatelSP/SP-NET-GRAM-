package com.spnetgram.app.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.chip.ChipGroup;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.theme.ThemeEngine;

public class ThemeSettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_theme_settings);
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);
        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setTitle("Themes"); }
        ThemeEngine te = SPNetGramApp.getInstance().getThemeEngine();
        ChipGroup cg = findViewById(R.id.chip_group_presets);
        // Each chip applies a preset on click
        int[] ids = {R.id.chip_default, R.id.chip_midnight, R.id.chip_rose_gold, R.id.chip_forest, R.id.chip_sunset, R.id.chip_arctic, R.id.chip_purple};
        ThemeEngine.ThemePreset[] presets = ThemeEngine.ThemePreset.values();
        for (int i = 0; i < ids.length && i < presets.length; i++) {
            final ThemeEngine.ThemePreset p = presets[i];
            View chip = cg.findViewById(ids[i]);
            if (chip != null) chip.setOnClickListener(v -> te.setPreset(p));
        }
    }
    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
