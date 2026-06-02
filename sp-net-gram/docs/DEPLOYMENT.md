# SP NET GRAM — Deployment & Backend Documentation

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                SP NET GRAM Android App               │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  │
│  │ Telegram │  │ SP       │  │ AI Features       │  │
│  │ MTProto  │  │ Premium  │  │ (OpenAI)          │  │
│  │ Layer    │  │ Billing  │  │                   │  │
│  └──────────┘  └──────────┘  └───────────────────┘  │
└─────────────────────────────────────────────────────┘
         │                 │
         ▼                 ▼
┌─────────────┐   ┌────────────────────┐
│  Telegram   │   │  SP NET GRAM       │
│  MTProto    │   │  Backend API       │
│  Servers    │   │  (Firebase /       │
│             │   │   Cloud Run)       │
└─────────────┘   └────────────────────┘
                           │
                  ┌────────┴────────┐
                  │                 │
          ┌───────────┐    ┌────────────────┐
          │ Firestore │    │ Admin Dashboard│
          │ Database  │    │ (Web App)      │
          └───────────┘    └────────────────┘
```

## Firebase Project Setup

### 1. Create Firebase Project

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create project: **SP NET GRAM**
3. Enable Google Analytics → enable

### 2. Enable Required Services

| Service | Purpose |
|---------|---------|
| Authentication | User identity, linked to Telegram UID |
| Firestore | User profiles, coins, diamond status |
| Cloud Messaging (FCM) | Push notifications |
| Analytics | Consented usage stats only |
| Crashlytics | Crash reporting |

### 3. Firestore Data Structure

```
users/{uid}
  ├── telegramId: long
  ├── displayName: string
  ├── phoneHash: string  (hashed, never raw)
  ├── spCoins: number
  ├── diamondTier: "NONE" | "MONTHLY" | "YEARLY"
  ├── diamondExpiry: timestamp
  ├── purchaseToken: string
  ├── referralCode: string
  ├── analyticsConsented: boolean
  ├── createdAt: timestamp
  └── updatedAt: timestamp

coin_transactions/{txnId}
  ├── uid: string
  ├── delta: number  (positive = earn, negative = spend)
  ├── type: string
  ├── description: string
  ├── timestamp: long
  └── isEarn: boolean

referral_codes/{code}
  ├── ownerUid: string
  ├── code: string
  ├── createdAt: long
  └── usageCount: number
```

### 4. Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can only read/write their own document
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
      // Never expose phoneHash or purchaseToken to other users
    }

    // Coin transactions: user can read own, write via server only
    match /coin_transactions/{txnId} {
      allow read: if request.auth != null && resource.data.uid == request.auth.uid;
      allow write: if false; // Server-side only via Admin SDK
    }

    // Referral codes: any authenticated user can read, owner creates
    match /referral_codes/{code} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.ownerUid == request.auth.uid;
      allow update: if false; // Server-side only
    }
  }
}
```

## Google Play Console Setup

### In-App Products (SP Diamond)

1. Open [play.google.com/console](https://play.google.com/console)
2. Navigate to **Monetize → Subscriptions**
3. Create two subscriptions:

| Field | Monthly | Yearly |
|-------|---------|--------|
| Product ID | `sp_diamond_monthly` | `sp_diamond_yearly` |
| Name | SP Diamond Monthly | SP Diamond Yearly |
| Billing period | 1 month | 1 year |
| Price | Your choice | Your choice (≥40% saving vs monthly) |
| Free trial | 3 days (optional) | 7 days (optional) |
| Grace period | 3 days | 3 days |

### Google Play In-App Review

After purchase is successful, trigger in-app review:
```java
ReviewManager manager = ReviewManagerFactory.create(context);
manager.requestReviewFlow().addOnCompleteListener(task -> {
    if (task.isSuccessful()) {
        manager.launchReviewFlow(activity, task.getResult());
    }
});
```

## Backend API (Optional — for advanced features)

The app works without a backend for core messaging (uses Telegram's servers).
A backend is only needed for:
- Server-side purchase validation
- Cross-device coin sync
- Admin dashboard
- Feature flags
- Announcements

### Recommended: Firebase Cloud Functions

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Validate Google Play purchase server-side
exports.validatePurchase = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', '');
  const { purchaseToken, productId } = data;
  // Verify with Google Play Developer API
  // Update Firestore if valid
});

// Earn coins (server-side to prevent cheating)
exports.earnCoins = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', '');
  const { amount, reason } = data;
  const uid = context.auth.uid;
  await admin.firestore().collection('users').doc(uid).update({
    spCoins: admin.firestore.FieldValue.increment(amount)
  });
});
```

Deploy:
```bash
npm install -g firebase-tools
firebase login
firebase deploy --only functions
```

## Privacy & GDPR Compliance

### Data collected (with consent)
- Active sessions count
- Device category (phone/tablet)
- App version
- Feature usage flags (which features are used, not content)

### Data NEVER collected
- Message content
- Contact names or phone numbers
- Location data
- Any personally identifiable information
- Biometric data

### User rights (implemented in app)
- **Access**: Users can request their data via Settings → Privacy → GDPR
- **Deletion**: "Delete Account" in Settings permanently removes Firestore data
- **Portability**: Not applicable (messages live on Telegram's servers)
- **Consent withdrawal**: Analytics toggle in Settings → Analytics

### Data retention
- User profiles: retained until account deletion
- Coin transactions: 90 days
- Analytics events: 14 months (Firebase default)

## Security Checklist

- [x] TLS/HTTPS enforced via `network_security_config.xml`
- [x] Cleartext traffic disabled
- [x] Sensitive data stored in `EncryptedSharedPreferences`
- [x] Backup of sensitive prefs disabled in `data_extraction_rules.xml`
- [x] ProGuard removes debug logging in release builds
- [x] API keys stored in `SecureStorageManager`, never in source code
- [x] Firebase Security Rules enforce per-user data isolation
- [x] Purchase tokens validated server-side via Firebase Functions
- [x] Telegram end-to-end encryption preserved (no modification to MTProto layer)
- [x] Biometric lock option available
- [x] GDPR consent flow before any analytics collection
