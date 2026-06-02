# SP NET GRAM — Build Instructions

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | Hedgehog 2023.1.1+ | [developer.android.com](https://developer.android.com/studio) |
| JDK | 17 or higher | [adoptium.net](https://adoptium.net) |
| Android SDK | API 35 | via Android Studio SDK Manager |
| Android NDK | r25c | via Android Studio SDK Manager |
| Git | any | [git-scm.com](https://git-scm.com) |

## Step 1 — Run the setup script

```bash
# Clone this repository
git clone https://github.com/your-org/sp-net-gram.git
cd sp-net-gram

# Make the setup script executable
chmod +x scripts/setup.sh

# Run it — it will:
#   • Clone the official Telegram Android source
#   • Rename all packages to com.spnetgram.app
#   • Rebrand all strings to SP NET GRAM
#   • Copy in all SP NET GRAM custom feature files
#   • Merge required dependencies
./scripts/setup.sh
```

After the script finishes you will have a complete `SPNetGram/` project directory.

## Step 2 — Configure API credentials

### 2a — Telegram API credentials (REQUIRED)
1. Go to [my.telegram.org](https://my.telegram.org) and log in.
2. Click **API development tools**.
3. Create a new app (or use existing).
4. Copy your **API ID** and **API Hash**.
5. Open `SPNetGram/gradle.properties` and add:

```properties
TELEGRAM_API_ID=YOUR_API_ID_HERE
TELEGRAM_API_HASH=YOUR_API_HASH_HERE
```

### 2b — Firebase (REQUIRED for push notifications, analytics, premium)
1. Go to the [Firebase Console](https://console.firebase.google.com).
2. Create a new project called **SP NET GRAM**.
3. Add an Android app with package name `com.spnetgram.app`.
4. Download `google-services.json`.
5. Place it at `SPNetGram/TMessagesProj/google-services.json`.

### 2c — Google Play Billing (REQUIRED for SP Diamond subscriptions)
1. Go to [Google Play Console](https://play.google.com/console).
2. Create your app listing.
3. Under **Monetize → Subscriptions**, create:
   - Product ID: `sp_diamond_monthly`
   - Product ID: `sp_diamond_yearly`
4. No code changes needed — the IDs are already configured.

### 2d — OpenAI API (OPTIONAL — for AI features)
Users configure their own OpenAI API key inside the app at:
**Settings → AI Features → OpenAI API Key**

No build-time configuration needed.

## Step 3 — Open in Android Studio

```
File → Open → SPNetGram/
```

Wait for Gradle sync to complete. This may take a few minutes on first run as it
downloads all dependencies.

If sync fails, check:
- JDK path: `File → Project Structure → SDK Location → JDK Location` → must be JDK 17
- Android SDK installed (API 35): `Tools → SDK Manager`
- NDK installed (r25c): `Tools → SDK Manager → SDK Tools → NDK (Side by side)`

## Step 4 — Signing config

For release builds you need a keystore. Generate one if you don't have one:

```bash
keytool -genkey -v \
  -keystore SPNetGram/keystore/spnetgram.jks \
  -alias spnetgram \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Then add to `SPNetGram/gradle.properties`:

```properties
KEYSTORE_PATH=keystore/spnetgram.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=spnetgram
KEY_PASSWORD=your_key_password
```

## Step 5 — Build

See `APK_GENERATION.md` for full build commands.

Quick build:
```bash
cd SPNetGram
./gradlew assembleRelease
```

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `NDK not found` | Install NDK r25c via SDK Manager |
| `google-services.json not found` | Place the file at `TMessagesProj/google-services.json` |
| `API_ID not set` | Add `TELEGRAM_API_ID` to `gradle.properties` |
| Gradle OOM | Add `-Xmx4g` to `~/.gradle/gradle.properties` |
| `Duplicate class` errors | Run `./gradlew clean` then rebuild |
| Build takes too long | Enable `org.gradle.parallel=true` in `gradle.properties` |
