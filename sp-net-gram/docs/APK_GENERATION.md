# SP NET GRAM — APK & AAB Generation Guide

Complete the steps in `BUILD.md` first.

---

## Build variants

SP NET GRAM uses two build flavors × two build types:

| Flavor | Description |
|--------|-------------|
| `universal` | Contains all ABIs — works on any Android device |
| `arm64` | ARM64-only — smaller APK, for modern flagship devices |

| Type | Description |
|------|-------------|
| `debug` | Unminified, with logging, package suffix `.debug` |
| `release` | Minified, signed, production-ready |

---

## 1 — Universal APK (recommended for distribution)

```bash
cd SPNetGram

# Build signed universal release APK
./gradlew :TMessagesProj:assembleUniversalRelease

# Output:
# TMessagesProj/build/outputs/apk/universal/release/
#   TMessagesProj-universal-release.apk
```

Install directly on a device:
```bash
adb install -r TMessagesProj/build/outputs/apk/universal/release/TMessagesProj-universal-release.apk
```

---

## 2 — ARM64-only APK (for sideloading on modern Android)

```bash
./gradlew :TMessagesProj:assembleArm64Release

# Output:
# TMessagesProj/build/outputs/apk/arm64/release/
#   TMessagesProj-arm64-release.apk
```

---

## 3 — Android App Bundle (AAB) — for Google Play upload

```bash
./gradlew :TMessagesProj:bundleUniversalRelease

# Output:
# TMessagesProj/build/outputs/bundle/universalRelease/
#   TMessagesProj-universal-release.aab
```

The AAB is the preferred format for Google Play. Play will generate optimised
per-device APKs from it automatically.

---

## 4 — All builds at once

```bash
./gradlew assembleRelease bundleRelease

# Produces all APK and AAB variants in one pass
```

---

## 5 — Debug APK (for testing)

```bash
./gradlew assembleUniversalDebug

# No signing config needed — uses debug keystore automatically
```

---

## 6 — Split APKs by ABI (advanced)

The `splits` block in `app/build.gradle` is already configured. Running
`assembleUniversalRelease` produces these split APKs automatically:

```
TMessagesProj-armeabi-v7a-release.apk   (32-bit ARM)
TMessagesProj-arm64-v8a-release.apk     (64-bit ARM)
TMessagesProj-x86-release.apk           (x86 emulators)
TMessagesProj-x86_64-release.apk        (x86_64 emulators)
TMessagesProj-universal-release.apk     (all architectures)
```

---

## Verify the APK signature

```bash
# Verify signature
apksigner verify --verbose TMessagesProj-universal-release.apk

# Check package name and version
aapt dump badging TMessagesProj-universal-release.apk | grep -E "package|versionName"
```

Expected output:
```
package: name='com.spnetgram.app' versionCode='1' versionName='1.0.0'
```

---

## Build output locations

```
SPNetGram/TMessagesProj/build/
├── outputs/
│   ├── apk/
│   │   ├── universal/
│   │   │   ├── debug/   → debug APKs
│   │   │   └── release/ → release APKs ← USE THESE
│   │   └── arm64/
│   │       └── release/ → ARM64 APK
│   └── bundle/
│       └── universalRelease/
│           └── *.aab    → AAB for Google Play ← USE FOR PLAY STORE
└── reports/             → lint + test reports
```

---

## CI/CD (GitHub Actions example)

```yaml
name: Build SP NET GRAM

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Run setup script
        run: chmod +x scripts/setup.sh && ./scripts/setup.sh

      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > SPNetGram/keystore/spnetgram.jks

      - name: Build release APK + AAB
        working-directory: SPNetGram
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          TELEGRAM_API_ID: ${{ secrets.TELEGRAM_API_ID }}
          TELEGRAM_API_HASH: ${{ secrets.TELEGRAM_API_HASH }}
        run: ./gradlew assembleUniversalRelease bundleUniversalRelease

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: sp-net-gram-apk
          path: SPNetGram/TMessagesProj/build/outputs/apk/universal/release/*.apk

      - name: Upload AAB
        uses: actions/upload-artifact@v4
        with:
          name: sp-net-gram-aab
          path: SPNetGram/TMessagesProj/build/outputs/bundle/universalRelease/*.aab
```
