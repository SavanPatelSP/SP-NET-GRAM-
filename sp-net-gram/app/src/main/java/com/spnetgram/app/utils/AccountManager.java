package com.spnetgram.app.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spnetgram.app.security.SecureStorageManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-account manager for SP NET GRAM.
 * Supports up to 5 simultaneous Telegram accounts with instant switching.
 */
public class AccountManager {

    private static final String TAG = "AccountManager";
    private static final int MAX_ACCOUNTS = 5;
    private static final String KEY_ACCOUNTS = "accounts_list";

    private static volatile AccountManager instance;
    private final Context context;
    private final SecureStorageManager secureStorage;
    private final Gson gson = new Gson();
    private List<Account> accounts;
    private int activeAccountIndex = 0;

    public static class Account {
        public String id;
        public String phoneNumber;
        public String displayName;
        public String avatarPath;
        public long userId;
        public boolean isActive;
        public long addedAt;
        public String datacenter;

        public Account(String id, String phoneNumber, String displayName, long userId) {
            this.id = id;
            this.phoneNumber = phoneNumber;
            this.displayName = displayName;
            this.userId = userId;
            this.addedAt = System.currentTimeMillis();
        }
    }

    private AccountManager(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = SecureStorageManager.getInstance(context);
        this.accounts = loadAccounts();
    }

    public static AccountManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager(context);
                }
            }
        }
        return instance;
    }

    public boolean addAccount(Account account) {
        if (accounts.size() >= MAX_ACCOUNTS) {
            Log.w(TAG, "Maximum accounts reached (" + MAX_ACCOUNTS + ")");
            return false;
        }
        accounts.add(account);
        saveAccounts();
        return true;
    }

    public boolean removeAccount(String accountId) {
        boolean removed = accounts.removeIf(a -> a.id.equals(accountId));
        if (removed) saveAccounts();
        return removed;
    }

    public void switchToAccount(int index) {
        if (index < 0 || index >= accounts.size()) return;
        for (int i = 0; i < accounts.size(); i++) {
            accounts.get(i).isActive = (i == index);
        }
        activeAccountIndex = index;
        secureStorage.putInt(SecureStorageManager.KEY_ACTIVE_ACCOUNT_ID, index);
        saveAccounts();
    }

    public Account getActiveAccount() {
        if (accounts.isEmpty()) return null;
        int idx = secureStorage.getInt(SecureStorageManager.KEY_ACTIVE_ACCOUNT_ID, 0);
        if (idx >= accounts.size()) idx = 0;
        return accounts.get(idx);
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    public int getAccountCount() { return accounts.size(); }
    public boolean canAddMore() { return accounts.size() < MAX_ACCOUNTS; }

    private void saveAccounts() {
        secureStorage.putString(KEY_ACCOUNTS, gson.toJson(accounts));
    }

    private List<Account> loadAccounts() {
        String json = secureStorage.getString(KEY_ACCOUNTS, null);
        if (json == null) return new ArrayList<>();
        try {
            Type type = new TypeToken<List<Account>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load accounts", e);
            return new ArrayList<>();
        }
    }
}
