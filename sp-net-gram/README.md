# SP NET GRAM — Advanced Telegram Client

A production-ready Android Telegram client with AI features, premium subscriptions, custom themes, and glassmorphism UI.

**Package:** `com.spnetgram.app`  
**Min SDK:** 21 (Android 5.0)  
**Target SDK:** 35 (Android 15)

---

## Quick Start

```bash
# 1. Clone this repo
git clone https://github.com/your-org/sp-net-gram.git
cd sp-net-gram

# 2. Run the setup script (clones Telegram base + applies all modifications)
chmod +x scripts/setup.sh
./scripts/setup.sh

# 3. Open SPNetGram/ in Android Studio
# 4. Add google-services.json + Telegram API credentials (see docs/BUILD.md)
# 5. Build APK
./gradlew assembleUniversalRelease
```

See **[docs/BUILD.md](docs/BUILD.md)** for the full setup guide.

---

## Project Structure

```
sp-net-gram/
├── app/
│   └── src/main/java/com/spnetgram/app/
│       ├── SPNetGramApp.java              # Application class
│       ├── ai/
│       │   ├── AIManager.java             # Central AI coordinator (OpenAI)
│       │   └── AIVoiceNoteHelper.java     # Whisper transcription + summary
│       ├── premium/
│       │   ├── SPDiamondManager.java      # Monthly/yearly subscription (Play Billing)
│       │   └── SPCoinsManager.java        # Virtual currency + referrals
│       ├── theme/
│       │   └── ThemeEngine.java           # Advanced theme engine + glassmorphism
│       ├── analytics/
│       │   └── AnalyticsManager.java      # GDPR-safe Firebase Analytics wrapper
│       ├── security/
│       │   └── SecureStorageManager.java  # EncryptedSharedPreferences wrapper
│       ├── network/
│       │   ├── ApiClient.java             # Retrofit client
│       │   └── SPNetGramApiService.java   # API endpoints
│       ├── utils/
│       │   ├── AccountManager.java        # Multi-account (up to 5)
│       │   └── NotificationHelper.java    # Notification channel setup
│       ├── service/
│       │   ├── MessagingService.java      # Background MTProto connection
│       │   └── SPFirebaseMessagingService.java  # FCM push handling
│       ├── receiver/
│       │   └── BootReceiver.java          # Auto-restart on boot
│       └── ui/
│           ├── MainActivity.java          # Bottom nav host
│           ├── SplashActivity.java        # Splash screen
│           ├── GDPRConsentActivity.java   # First-run consent
│           ├── auth/                      # Phone + code + password auth
│           ├── chat/                      # Chat list + individual chat + AI
│           ├── premium/                   # SP Diamond + SP Coins screens
│           ├── settings/                  # All settings screens
│           ├── contacts/                  # Contacts list
│           └── calls/                     # Call history
├── scripts/
│   └── setup.sh                           # One-command project setup
└── docs/
    ├── BUILD.md                           # Full build instructions
    ├── APK_GENERATION.md                  # APK + AAB generation guide
    └── DEPLOYMENT.md                      # Backend + Firebase + Play setup
```

---

## Features

### Messaging
- ✅ Full Telegram MTProto protocol (preserved from base)
- ✅ Multi-account support (up to 5 simultaneous accounts)
- ✅ Scheduled messages
- ✅ Silent messages (no notification to recipient)
- ✅ Quick reply system
- ✅ Folder management (All / Personal / Work / Channels / Bots)
- ✅ Advanced search

### AI Features (powered by OpenAI)
- ✅ AI message summarizer — summarize long chats in seconds
- ✅ AI chat translator — translate any message to any language
- ✅ AI rewrite assistant — 7 rewrite styles (formal, casual, shorter, longer, polite, assertive, funny)
- ✅ AI voice note summaries — Whisper transcription + summary
- ✅ AI smart replies — 3 contextual reply suggestions

> AI features use the user's own OpenAI API key, configured in Settings → AI Features.

### SP Diamond (Premium)
- ✅ Monthly and yearly subscription plans via Google Play Billing
- ✅ Special diamond badge on profile
- ✅ Unlocks all AI features without coin cost
- ✅ Access to exclusive premium themes
- ✅ Unlimited animated stickers
- ✅ Advanced folder management
- ✅ No ads

### SP Coins (Virtual Currency)
- ✅ Earn coins via daily login, referrals, activity, streaks
- ✅ Spend coins on AI features, theme unlocks, sticker packs
- ✅ Referral system — generates unique referral codes
- ✅ Real-time balance sync via Firestore

### UI / UX
- ✅ Glassmorphism effects (blur, frosted glass)
- ✅ Advanced theme engine with 7 presets + custom accent color
- ✅ Light and dark modes + follow system
- ✅ Custom chat bubble styles (5 styles)
- ✅ Custom chat backgrounds (solid / gradient / wallpaper)
- ✅ Smooth animations

### Security & Privacy
- ✅ Telegram end-to-end encryption fully preserved
- ✅ EncryptedSharedPreferences for all sensitive local data
- ✅ Biometric app lock
- ✅ GDPR consent flow on first launch
- ✅ Analytics only collected with explicit consent
- ✅ Message content NEVER collected or transmitted to SP servers
- ✅ Network security config — TLS enforced, cleartext disabled
- ✅ Backup of sensitive data disabled

---

## Build outputs

| File | Description |
|------|-------------|
| `*-universal-release.apk` | Works on all Android devices |
| `*-arm64-v8a-release.apk` | ARM64 only (smaller, for flagships) |
| `*-universal-release.aab` | Android App Bundle for Google Play |

See **[docs/APK_GENERATION.md](docs/APK_GENERATION.md)** for all build commands.

---

## Required credentials

| Credential | Where to get it |
|-----------|----------------|
| Telegram API ID + Hash | [my.telegram.org](https://my.telegram.org) |
| `google-services.json` | [Firebase Console](https://console.firebase.google.com) |
| Google Play Billing products | [Play Console](https://play.google.com/console) |
| OpenAI API key | [platform.openai.com](https://platform.openai.com) — entered by user in-app |

---

## License

This project is based on the [official Telegram Android client](https://github.com/DrKLO/Telegram)
which is licensed under the GNU General Public License v2.0.

All SP NET GRAM additions and modifications are also subject to the GPL v2.0.

The Telegram name and logo are trademarks of Telegram FZ-LLC. SP NET GRAM is an
independent third-party client and is not affiliated with or endorsed by Telegram.
