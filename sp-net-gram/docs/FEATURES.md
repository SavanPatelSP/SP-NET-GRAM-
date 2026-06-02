# SP NET GRAM — Feature Reference

## AI Features Integration Guide

All AI features go through `AIManager.java`. The manager requires an OpenAI API key
stored in `SecureStorageManager.KEY_OPENAI_API_KEY`.

### Chat Summarizer
```java
AIManager.getInstance(context).summarizeMessages(
    messages,   // concatenated message text
    "English",  // target language for the summary
    new AIManager.AICallback() {
        @Override public void onSuccess(String summary) { /* show summary */ }
        @Override public void onError(String error) { /* handle error */ }
    }
);
```

### Message Translator
```java
AIManager.getInstance(context).translateMessage(
    "Bonjour!",
    "English",
    callback
);
```

### Message Rewriter
```java
AIManager.getInstance(context).rewriteMessage(
    "i need this asap",
    AIManager.RewriteStyle.FORMAL,
    callback
);
// Result: "I would appreciate your prompt attention to this matter."
```

### Voice Note Summary
```java
AIVoiceNoteHelper helper = new AIVoiceNoteHelper(context);
helper.transcribeAndSummarize(audioFile, new AIVoiceNoteHelper.VoiceNoteCallback() {
    @Override public void onTranscriptionReady(String transcript) { /* optional: show transcript */ }
    @Override public void onSummaryReady(String summary) { /* show summary */ }
    @Override public void onError(String error) { /* handle */ }
});
```

### Smart Replies
```java
AIManager.getInstance(context).generateSmartReplies(
    conversationContext, // last few messages as text
    3,                   // number of reply suggestions
    new AIManager.AICallback() {
        @Override public void onSuccess(String result) {
            // result is "Reply 1||Reply 2||Reply 3"
            String[] replies = result.split("\\|\\|");
        }
        @Override public void onError(String error) {}
    }
);
```

---

## SP Coins — Earning & Spending

### Earn coins
```java
SPCoinsManager coins = SPNetGramApp.getInstance().getCoinsManager();

// Daily login reward (rate-limited to once per 24h)
coins.dailyLoginReward(callback);

// Manual earn (for custom reward triggers)
coins.earnCoins(
    SPCoinsManager.REWARD_VOICE_NOTE_SENT,
    SPCoinsManager.TransactionType.EARN_ACTIVITY,
    "Voice note sent",
    callback
);

// Referral reward
coins.referralReward("SP1A2B3C", callback);
```

### Spend coins
```java
coins.spendCoins(
    SPCoinsManager.COST_AI_SUMMARY,
    SPCoinsManager.TransactionType.SPEND_AI,
    "Chat summarize",
    callback
);

// Always check canAfford() before spending
if (!coins.canAfford(SPCoinsManager.COST_AI_SUMMARY)) {
    // Show "not enough coins" UI
}
```

### Reward amounts

| Trigger | Coins |
|---------|-------|
| Daily login | +10 |
| Referral (both parties) | +100 |
| First message sent | +5 |
| Profile complete | +50 |
| Share app | +20 |
| Voice note sent | +2 |
| Weekly streak | +50 |

### Spend costs

| Feature | Coins |
|---------|-------|
| AI chat summary | 5 |
| AI translate | 3 |
| AI rewrite | 3 |
| Custom sticker pack | 200 |
| Theme unlock | 150 |

> SP Diamond users never pay coins for AI features.

---

## Theme Engine

### Apply a preset
```java
ThemeEngine.getInstance(context).setPreset(ThemeEngine.ThemePreset.MIDNIGHT);
```

### Toggle dark mode
```java
ThemeEngine.getInstance(context).setMode(ThemeEngine.Mode.DARK);
```

### Enable glassmorphism
```java
ThemeEngine.getInstance(context).setGlassmorphismEnabled(true);
```

### Get blur radius for a surface
```java
int blurRadius = ThemeEngine.getInstance(context).getBlurRadius(ThemeEngine.GlassLayer.CHAT_HEADER);
// Apply with Blurry library:
Blurry.with(context).radius(blurRadius).onto(targetView);
```

### Available presets

| Preset | Primary | Description |
|--------|---------|-------------|
| `DEFAULT_BLUE` | #1565C0 | Classic Telegram blue |
| `MIDNIGHT` | #311B92 | Deep purple dark theme |
| `ROSE_GOLD` | #B76E79 | Rose gold accent |
| `FOREST_GREEN` | #1B5E20 | Nature green |
| `SUNSET_ORANGE` | #E65100 | Warm orange |
| `ARCTIC` | #006064 | Cool teal |
| `PURPLE_HAZE` | #6A1B9A | Soft purple |
| `CUSTOM` | any | User-picked accent color |

---

## Analytics — Privacy Guide

`AnalyticsManager` wraps Firebase Analytics with a hard privacy gate.

```java
AnalyticsManager analytics = SPNetGramApp.getInstance().getAnalyticsManager();

// Only logs if user has consented
analytics.logAIFeatureUsed("chat_summarize");
analytics.logDiamondPurchased("MONTHLY");
analytics.logCoinsEarned(10, "daily_login");
analytics.logThemeChanged("midnight");
```

### What is NEVER logged
The `containsPii()` guard blocks any bundle that contains keys matching:
`phone`, `email`, `name`, `message`, `content`, `text`, `uid`, `id`

### Set consent
```java
analytics.setConsent(true);  // user opted in
analytics.setConsent(false); // user opted out — disables Firebase collection entirely
```

---

## Security — SecureStorageManager

All sensitive values use `EncryptedSharedPreferences` (AES256-GCM + AES256-SIV):

```java
SecureStorageManager storage = SecureStorageManager.getInstance(context);

// Store OpenAI key
storage.putString(SecureStorageManager.KEY_OPENAI_API_KEY, "sk-...");

// Read back
String key = storage.getString(SecureStorageManager.KEY_OPENAI_API_KEY, "");
```

Available keys: see `SecureStorageManager.java` for the full constant list.

---

## Multi-Account

```java
AccountManager accounts = SPNetGramApp.getInstance().getAccountManager();

// Add account (max 5)
AccountManager.Account acc = new AccountManager.Account(id, phone, name, userId);
accounts.addAccount(acc);

// Switch active account
accounts.switchToAccount(1); // index 0–4

// Get active
AccountManager.Account active = accounts.getActiveAccount();
```
