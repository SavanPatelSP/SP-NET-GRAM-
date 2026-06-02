package com.spnetgram.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.spnetgram.app.R;
import com.spnetgram.app.SPNetGramApp;
import com.spnetgram.app.security.SecureStorageManager;
import com.spnetgram.app.ui.auth.PhoneAuthActivity;
import com.spnetgram.app.ui.chat.ChatListFragment;
import com.spnetgram.app.ui.contacts.ContactsFragment;
import com.spnetgram.app.ui.calls.CallsFragment;
import com.spnetgram.app.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNav;
    private SecureStorageManager secureStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        secureStorage = SPNetGramApp.getInstance().getSecureStorage();

        // Check if user has completed onboarding / auth
        if (!secureStorage.getBoolean(SecureStorageManager.KEY_ONBOARDING_DONE, false)) {
            startActivity(new Intent(this, PhoneAuthActivity.class));
            finish();
            return;
        }

        // Check GDPR consent
        if (!secureStorage.getBoolean(SecureStorageManager.KEY_GDPR_ACCEPTED, false)) {
            startActivity(new Intent(this, GDPRConsentActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new ChatListFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(this);
        applyGlassmorphismToBottomNav();
    }

    private void applyGlassmorphismToBottomNav() {
        if (SPNetGramApp.getInstance().getThemeEngine().isGlassmorphismEnabled()) {
            bottomNav.setAlpha(SPNetGramApp.getInstance().getThemeEngine()
                .getGlassAlpha(com.spnetgram.app.theme.ThemeEngine.GlassLayer.BOTTOM_BAR));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_chats) {
            loadFragment(new ChatListFragment());
            return true;
        } else if (id == R.id.nav_contacts) {
            loadFragment(new ContactsFragment());
            return true;
        } else if (id == R.id.nav_calls) {
            loadFragment(new CallsFragment());
            return true;
        } else if (id == R.id.nav_settings) {
            loadFragment(new SettingsFragment());
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;
        String path = intent.getData().getPath();
        if (path != null && path.startsWith("/resolve/")) {
            String username = path.replace("/resolve/", "");
            // Open profile/chat for username
        }
    }
}
