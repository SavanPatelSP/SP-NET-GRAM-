package com.spnetgram.app.premium;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.spnetgram.app.security.SecureStorageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SP Coins — Virtual currency system.
 * Rewards, referrals, and in-app spending.
 */
public class SPCoinsManager {

    private static final String TAG = "SPCoinsManager";

    // Reward amounts
    public static final int REWARD_DAILY_LOGIN        = 10;
    public static final int REWARD_REFERRAL           = 100;
    public static final int REWARD_FIRST_MESSAGE      = 5;
    public static final int REWARD_PROFILE_COMPLETE   = 50;
    public static final int REWARD_SHARE_APP          = 20;
    public static final int REWARD_VOICE_NOTE_SENT    = 2;
    public static final int REWARD_WEEK_STREAK        = 50;

    // Spend amounts
    public static final int COST_AI_SUMMARY          = 5;
    public static final int COST_AI_TRANSLATE        = 3;
    public static final int COST_AI_REWRITE          = 3;
    public static final int COST_CUSTOM_STICKER_PACK = 200;
    public static final int COST_THEME_UNLOCK        = 150;

    public enum TransactionType {
        EARN_DAILY_LOGIN, EARN_REFERRAL, EARN_ACTIVITY,
        EARN_STREAK, EARN_MANUAL,
        SPEND_AI, SPEND_THEME, SPEND_STICKER, SPEND_OTHER
    }

    private static volatile SPCoinsManager instance;

    private final Context context;
    private final SecureStorageManager secureStorage;
    private final FirebaseFirestore db;
    private long cachedBalance = 0;
    private ListenerRegistration balanceListener;
    private final List<CoinsBalanceListener> listeners = new ArrayList<>();

    private SPCoinsManager(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = SecureStorageManager.getInstance(context);
        this.db = FirebaseFirestore.getInstance();
        cachedBalance = secureStorage.getLong(SecureStorageManager.KEY_COINS_BALANCE, 0);
        startRealtimeListener();
    }

    public static SPCoinsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SPCoinsManager.class) {
                if (instance == null) {
                    instance = new SPCoinsManager(context);
                }
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Balance
    // ──────────────────────────────────────────────────────────────────────────

    public long getBalance() {
        return cachedBalance;
    }

    public void fetchBalance(BalanceFetchCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onResult(0); return; }

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener(doc -> {
                long balance = doc.getLong("spCoins") != null ? doc.getLong("spCoins") : 0;
                updateCachedBalance(balance);
                callback.onResult(balance);
            })
            .addOnFailureListener(e -> callback.onResult(cachedBalance));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Earn
    // ──────────────────────────────────────────────────────────────────────────

    public void earnCoins(int amount, TransactionType type, String description, CoinsCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Not logged in."); return; }

        Map<String, Object> txn = buildTransaction(amount, type, description, true);

        db.runTransaction(txnTask -> {
            DocumentSnapshot doc = txnTask.get(db.collection("users").document(uid));
            long current = doc.getLong("spCoins") != null ? doc.getLong("spCoins") : 0;
            txnTask.update(db.collection("users").document(uid),
                "spCoins", current + amount);
            txnTask.set(db.collection("coin_transactions").document(), txn);
            return current + amount;
        }).addOnSuccessListener(newBalance -> {
            updateCachedBalance((Long) newBalance);
            callback.onSuccess((Long) newBalance);
        }).addOnFailureListener(e -> callback.onError("Transaction failed: " + e.getMessage()));
    }

    public void dailyLoginReward(CoinsCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Not logged in."); return; }

        long lastReward = secureStorage.getLong(SecureStorageManager.KEY_LAST_DAILY_REWARD, 0);
        long now = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000L;

        if (now - lastReward < oneDayMs) {
            callback.onError("Daily reward already claimed today.");
            return;
        }

        secureStorage.putLong(SecureStorageManager.KEY_LAST_DAILY_REWARD, now);
        earnCoins(REWARD_DAILY_LOGIN, TransactionType.EARN_DAILY_LOGIN, "Daily login reward", callback);
    }

    public void referralReward(String referralCode, CoinsCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Not logged in."); return; }

        // Validate referral code server-side
        db.collection("referral_codes").document(referralCode)
            .get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    callback.onError("Invalid referral code.");
                    return;
                }

                String referrerUid = doc.getString("ownerUid");
                if (uid.equals(referrerUid)) {
                    callback.onError("Cannot use your own referral code.");
                    return;
                }

                // Reward both parties
                earnCoins(REWARD_REFERRAL, TransactionType.EARN_REFERRAL,
                    "Referral bonus (code: " + referralCode + ")", callback);

                // Reward referrer too
                Map<String, Object> referrerUpdate = new HashMap<>();
                referrerUpdate.put("spCoins", FieldValue.increment(REWARD_REFERRAL));
                db.collection("users").document(referrerUid).update(referrerUpdate);
            })
            .addOnFailureListener(e -> callback.onError("Failed to validate referral code."));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Spend
    // ──────────────────────────────────────────────────────────────────────────

    public void spendCoins(int amount, TransactionType type, String description, CoinsCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Not logged in."); return; }

        if (cachedBalance < amount) {
            callback.onError("Insufficient SP Coins. You need " + amount + " but have " + cachedBalance + ".");
            return;
        }

        Map<String, Object> txn = buildTransaction(-amount, type, description, false);

        db.runTransaction(txnTask -> {
            DocumentSnapshot doc = txnTask.get(db.collection("users").document(uid));
            long current = doc.getLong("spCoins") != null ? doc.getLong("spCoins") : 0;
            if (current < amount) throw new RuntimeException("Insufficient balance.");
            txnTask.update(db.collection("users").document(uid),
                "spCoins", current - amount);
            txnTask.set(db.collection("coin_transactions").document(), txn);
            return current - amount;
        }).addOnSuccessListener(newBalance -> {
            updateCachedBalance((Long) newBalance);
            callback.onSuccess((Long) newBalance);
        }).addOnFailureListener(e -> callback.onError("Spend failed: " + e.getMessage()));
    }

    public boolean canAfford(int amount) {
        return cachedBalance >= amount;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Referral code generation
    // ──────────────────────────────────────────────────────────────────────────

    public void generateReferralCode(ReferralCodeCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError("Not logged in."); return; }

        String existing = secureStorage.getString(SecureStorageManager.KEY_REFERRAL_CODE, null);
        if (existing != null) {
            callback.onCode(existing);
            return;
        }

        String code = "SP" + uid.substring(0, 6).toUpperCase();

        Map<String, Object> data = new HashMap<>();
        data.put("ownerUid", uid);
        data.put("code", code);
        data.put("createdAt", System.currentTimeMillis());
        data.put("usageCount", 0);

        db.collection("referral_codes").document(code)
            .set(data)
            .addOnSuccessListener(v -> {
                secureStorage.putString(SecureStorageManager.KEY_REFERRAL_CODE, code);
                callback.onCode(code);
            })
            .addOnFailureListener(e -> callback.onError("Failed to create referral code."));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────────────────────────────────

    private void startRealtimeListener() {
        String uid = getCurrentUid();
        if (uid == null) return;

        balanceListener = db.collection("users").document(uid)
            .addSnapshotListener((doc, e) -> {
                if (e != null || doc == null) return;
                Long balance = doc.getLong("spCoins");
                if (balance != null) updateCachedBalance(balance);
            });
    }

    private void updateCachedBalance(long balance) {
        cachedBalance = balance;
        secureStorage.putLong(SecureStorageManager.KEY_COINS_BALANCE, balance);
        for (CoinsBalanceListener l : listeners) l.onBalanceChanged(balance);
    }

    private Map<String, Object> buildTransaction(int delta, TransactionType type,
                                                  String description, boolean isEarn) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", getCurrentUid());
        data.put("delta", delta);
        data.put("type", type.name());
        data.put("description", description);
        data.put("timestamp", System.currentTimeMillis());
        data.put("isEarn", isEarn);
        return data;
    }

    private String getCurrentUid() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    public void addListener(CoinsBalanceListener l) { listeners.add(l); }
    public void removeListener(CoinsBalanceListener l) { listeners.remove(l); }

    public interface CoinsCallback {
        void onSuccess(long newBalance);
        void onError(String error);
    }

    public interface BalanceFetchCallback {
        void onResult(long balance);
    }

    public interface ReferralCodeCallback {
        void onCode(String code);
        void onError(String error);
    }

    public interface CoinsBalanceListener {
        void onBalanceChanged(long newBalance);
    }
}
