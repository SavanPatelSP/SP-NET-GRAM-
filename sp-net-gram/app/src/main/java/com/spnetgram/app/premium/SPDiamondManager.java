package com.spnetgram.app.premium;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spnetgram.app.security.SecureStorageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SP Diamond — Premium subscription manager.
 * Plans: monthly (sp_diamond_monthly) and yearly (sp_diamond_yearly).
 * Manages Google Play Billing, badge activation, and feature unlocking.
 */
public class SPDiamondManager implements PurchasesUpdatedListener {

    private static final String TAG = "SPDiamondManager";

    public static final String PRODUCT_MONTHLY = "sp_diamond_monthly";
    public static final String PRODUCT_YEARLY  = "sp_diamond_yearly";

    public enum DiamondTier { NONE, MONTHLY, YEARLY }

    public static final String FEATURE_CUSTOM_THEMES     = "custom_themes";
    public static final String FEATURE_AI_FEATURES       = "ai_features";
    public static final String FEATURE_NO_ADS            = "no_ads";
    public static final String FEATURE_DIAMOND_BADGE     = "diamond_badge";
    public static final String FEATURE_PRIORITY_SUPPORT  = "priority_support";
    public static final String FEATURE_ANIMATED_STICKERS = "animated_stickers_unlimited";
    public static final String FEATURE_VOICE_TO_TEXT     = "voice_to_text";
    public static final String FEATURE_FOLDER_MANAGEMENT = "folder_management_advanced";

    private static volatile SPDiamondManager instance;

    private final Context context;
    private final SecureStorageManager secureStorage;
    private final FirebaseFirestore db;
    private BillingClient billingClient;
    private DiamondTier currentTier = DiamondTier.NONE;
    private final List<DiamondStatusListener> listeners = new ArrayList<>();

    private SPDiamondManager(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = SecureStorageManager.getInstance(context);
        this.db = FirebaseFirestore.getInstance();
        setupBillingClient();
        loadCachedStatus();
    }

    public static SPDiamondManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SPDiamondManager.class) {
                if (instance == null) {
                    instance = new SPDiamondManager(context);
                }
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Billing setup
    // ──────────────────────────────────────────────────────────────────────────

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing client connected");
                    queryExistingPurchases();
                } else {
                    Log.w(TAG, "Billing setup failed: " + result.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected — will retry on next request");
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Launch purchase flow
    // ──────────────────────────────────────────────────────────────────────────

    public void launchSubscription(Activity activity, String productId, PurchaseCallback callback) {
        if (!billingClient.isReady()) {
            setupBillingClient();
            callback.onError("Billing not ready. Please try again.");
            return;
        }

        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        products.add(QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.SUBS)
            .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK
                    || productDetailsList.isEmpty()) {
                callback.onError("Product not found: " + productId);
                return;
            }

            ProductDetails productDetails = productDetailsList.get(0);
            List<ProductDetails.SubscriptionOfferDetails> offers =
                productDetails.getSubscriptionOfferDetails();
            if (offers == null || offers.isEmpty()) {
                callback.onError("No subscription offers available.");
                return;
            }

            List<BillingFlowParams.ProductDetailsParams> detailsParamsList = new ArrayList<>();
            detailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offers.get(0).getOfferToken())
                .build());

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(detailsParamsList)
                .build();

            BillingResult launchResult = billingClient.launchBillingFlow(activity, flowParams);
            if (launchResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                callback.onError("Could not start purchase: " + launchResult.getDebugMessage());
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Purchase handling
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult result,
                                    List<Purchase> purchases) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (result.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User cancelled purchase");
        } else {
            Log.w(TAG, "Purchase error: " + result.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;

        List<String> products = purchase.getProducts();
        DiamondTier newTier = DiamondTier.NONE;
        if (products.contains(PRODUCT_YEARLY)) {
            newTier = DiamondTier.YEARLY;
        } else if (products.contains(PRODUCT_MONTHLY)) {
            newTier = DiamondTier.MONTHLY;
        }

        if (newTier != DiamondTier.NONE) {
            setDiamondTier(newTier);
            syncToFirestore(purchase.getPurchaseToken(), newTier);
        }

        // Acknowledge purchase
        if (!purchase.isAcknowledged()) {
            billingClient.acknowledgePurchase(
                com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build(),
                ackResult -> Log.i(TAG, "Purchase acknowledged: " + ackResult.getResponseCode())
            );
        }
    }

    private void queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            (result, purchases) -> {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase p : purchases) {
                        if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            handlePurchase(p);
                        }
                    }
                }
            }
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Feature checks
    // ──────────────────────────────────────────────────────────────────────────

    public boolean isDiamondActive() {
        return currentTier != DiamondTier.NONE;
    }

    public boolean hasFeature(String featureKey) {
        return isDiamondActive();
    }

    public DiamondTier getCurrentTier() {
        return currentTier;
    }

    public String getBadgeLabel() {
        switch (currentTier) {
            case YEARLY:  return "💎 Diamond";
            case MONTHLY: return "💎 Diamond";
            default:      return "";
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────────────────────────────────

    private void setDiamondTier(DiamondTier tier) {
        currentTier = tier;
        secureStorage.putString(SecureStorageManager.KEY_DIAMOND_TIER, tier.name());
        notifyListeners(tier);
    }

    private void loadCachedStatus() {
        String cached = secureStorage.getString(SecureStorageManager.KEY_DIAMOND_TIER, DiamondTier.NONE.name());
        try {
            currentTier = DiamondTier.valueOf(cached);
        } catch (Exception e) {
            currentTier = DiamondTier.NONE;
        }
    }

    private void syncToFirestore(String purchaseToken, DiamondTier tier) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("diamondTier", tier.name());
        data.put("purchaseToken", purchaseToken);
        data.put("updatedAt", System.currentTimeMillis());

        db.collection("users").document(uid)
            .update(data)
            .addOnFailureListener(e -> Log.e(TAG, "Firestore sync failed", e));
    }

    private void notifyListeners(DiamondTier tier) {
        for (DiamondStatusListener l : listeners) {
            l.onDiamondStatusChanged(tier);
        }
    }

    public void addListener(DiamondStatusListener l) { listeners.add(l); }
    public void removeListener(DiamondStatusListener l) { listeners.remove(l); }

    public interface DiamondStatusListener {
        void onDiamondStatusChanged(DiamondTier tier);
    }

    public interface PurchaseCallback {
        void onPurchaseSuccess(DiamondTier tier);
        void onError(String message);
    }
}
