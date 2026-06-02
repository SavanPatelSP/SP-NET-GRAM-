package com.spnetgram.app.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.analytics.AnalyticsManager;
import com.spnetgram.app.theme.ThemeEngine;
import com.spnetgram.app.ui.premium.SPCoinsActivity;
import com.spnetgram.app.ui.premium.SPDiamondActivity;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchGlassmorphism;
    private SwitchMaterial switchAnalytics;
    private SwitchMaterial switchAppLock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        bindViews(view);
        setupThemeSection(view);
        setupPremiumSection(view);
        setupAISection(view);
        setupPrivacySection(view);
        setupNotificationsSection(view);
        setupAnalyticsToggle();

        return view;
    }

    private void bindViews(View view) {
        switchDarkMode       = view.findViewById(R.id.switch_dark_mode);
        switchGlassmorphism  = view.findViewById(R.id.switch_glassmorphism);
        switchAnalytics      = view.findViewById(R.id.switch_analytics);
        switchAppLock        = view.findViewById(R.id.switch_app_lock);
    }

    private void setupThemeSection(View view) {
        ThemeEngine theme = SPNetGramApp.getInstance().getThemeEngine();

        switchDarkMode.setChecked(theme.isDarkMode());
        switchDarkMode.setOnCheckedChangeListener((b, checked) -> {
            theme.setMode(checked ? ThemeEngine.Mode.DARK : ThemeEngine.Mode.LIGHT);
            SPNetGramApp.getInstance().getAnalyticsManager()
                .logThemeChanged(checked ? "dark" : "light");
        });

        switchGlassmorphism.setChecked(theme.isGlassmorphismEnabled());
        switchGlassmorphism.setOnCheckedChangeListener((b, checked) ->
            theme.setGlassmorphismEnabled(checked));

        view.findViewById(R.id.row_theme_presets).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), ThemeSettingsActivity.class)));

        view.findViewById(R.id.row_chat_background).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), ChatBackgroundActivity.class)));
    }

    private void setupPremiumSection(View view) {
        view.findViewById(R.id.row_sp_diamond).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), SPDiamondActivity.class)));

        view.findViewById(R.id.row_sp_coins).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), SPCoinsActivity.class)));
    }

    private void setupAISection(View view) {
        view.findViewById(R.id.row_ai_settings).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), AISettingsActivity.class)));
    }

    private void setupPrivacySection(View view) {
        view.findViewById(R.id.row_privacy).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), PrivacySettingsActivity.class)));

        view.findViewById(R.id.row_gdpr).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), GDPRDetailActivity.class)));
    }

    private void setupNotificationsSection(View view) {
        view.findViewById(R.id.row_notifications).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), NotificationSettingsActivity.class)));
    }

    private void setupAnalyticsToggle() {
        AnalyticsManager analytics = SPNetGramApp.getInstance().getAnalyticsManager();
        switchAnalytics.setChecked(analytics.isConsented());
        switchAnalytics.setOnCheckedChangeListener((b, checked) ->
            analytics.setConsent(checked));
    }
}
