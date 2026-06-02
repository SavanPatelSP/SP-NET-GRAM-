#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════════
#  SP NET GRAM — Project Setup Script
#  Clones the official Telegram Android source, applies all SP NET GRAM
#  modifications, copies in new feature files, and verifies the build.
# ═══════════════════════════════════════════════════════════════════════════════

set -euo pipefail

TELEGRAM_REPO="https://github.com/DrKLO/Telegram.git"
WORK_DIR="$(pwd)"
TELEGRAM_DIR="$WORK_DIR/TelegramBase"
SP_GRAM_DIR="$WORK_DIR/sp-net-gram"
OUTPUT_DIR="$WORK_DIR/SPNetGram"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log()  { echo -e "${GREEN}[SP NET GRAM]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
err()  { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }
step() { echo -e "\n${BLUE}━━━ Step $1 ━━━${NC}"; }

# ─── Prerequisites check ──────────────────────────────────────────────────────
step "1/8 — Checking prerequisites"
command -v git   >/dev/null 2>&1 || err "git is required"
command -v java  >/dev/null 2>&1 || err "Java 17+ is required (install from adoptium.net)"
JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
[ "$JAVA_VER" -ge 17 ] 2>/dev/null || warn "Java 17+ is recommended (found: $JAVA_VER)"
log "Prerequisites OK"

# ─── Clone Telegram Android base ─────────────────────────────────────────────
step "2/8 — Cloning Telegram Android base"
if [ -d "$TELEGRAM_DIR" ]; then
    log "Telegram base already cloned — pulling latest"
    git -C "$TELEGRAM_DIR" pull --rebase || warn "Pull failed, continuing with existing"
else
    git clone --depth=1 "$TELEGRAM_REPO" "$TELEGRAM_DIR"
    log "Telegram base cloned"
fi

# ─── Create output project ────────────────────────────────────────────────────
step "3/8 — Creating SP NET GRAM project structure"
rm -rf "$OUTPUT_DIR"
cp -r "$TELEGRAM_DIR" "$OUTPUT_DIR"
log "Base project copied to $OUTPUT_DIR"

# ─── Rebrand: package name ────────────────────────────────────────────────────
step "4/8 — Applying rebranding (package name + app name)"

OLD_PACKAGE="org.telegram.messenger"
NEW_PACKAGE="com.spnetgram.app"
OLD_PACKAGE_PATH="org/telegram/messenger"
NEW_PACKAGE_PATH="com/spnetgram/app"

log "Renaming Java package: $OLD_PACKAGE → $NEW_PACKAGE"

# Rename package declarations in all Java files
find "$OUTPUT_DIR" -name "*.java" -o -name "*.kt" | while read -r f; do
    sed -i "s/$OLD_PACKAGE/$NEW_PACKAGE/g" "$f"
done

# Rename package in Kotlin files too
find "$OUTPUT_DIR" -name "*.kt" | while read -r f; do
    sed -i "s/$OLD_PACKAGE/$NEW_PACKAGE/g" "$f"
done

# Rename in XML/Gradle/other config files
find "$OUTPUT_DIR" -name "*.xml" -o -name "*.gradle" -o -name "*.properties" \
    -o -name "*.json" -o -name "*.pro" | while read -r f; do
    sed -i "s/$OLD_PACKAGE/$NEW_PACKAGE/g" "$f" 2>/dev/null || true
done

# Move Java source directories
JAVA_SRC="$OUTPUT_DIR/TMessagesProj/src/main/java"
if [ -d "$JAVA_SRC/$OLD_PACKAGE_PATH" ]; then
    mkdir -p "$JAVA_SRC/$NEW_PACKAGE_PATH"
    cp -r "$JAVA_SRC/$OLD_PACKAGE_PATH/." "$JAVA_SRC/$NEW_PACKAGE_PATH/"
    rm -rf "$JAVA_SRC/org"
    log "Package directories moved"
fi

log "Package rename complete"

# ─── Rebrand: app name + strings ─────────────────────────────────────────────
step "5/8 — Rebranding strings and app name"

STRINGS_FILE="$OUTPUT_DIR/TMessagesProj/src/main/res/values/strings.xml"
if [ -f "$STRINGS_FILE" ]; then
    sed -i 's|<string name="app_name">Telegram</string>|<string name="app_name">SP NET GRAM</string>|g' "$STRINGS_FILE"
    sed -i 's|<string name="app_name">Telegram Beta</string>|<string name="app_name">SP NET GRAM</string>|g' "$STRINGS_FILE"
    sed -i 's|>Telegram<|>SP NET GRAM<|g' "$STRINGS_FILE"
    log "App name rebranded"
fi

# Rebrand all occurrences in strings
find "$OUTPUT_DIR/TMessagesProj/src/main/res" -name "strings*.xml" | while read -r f; do
    sed -i 's/Telegram/SP NET GRAM/g' "$f"
done

# Update build.gradle applicationId
find "$OUTPUT_DIR" -name "build.gradle" | while read -r f; do
    sed -i 's/applicationId "org.telegram.messenger"/applicationId "com.spnetgram.app"/g' "$f"
    sed -i 's/applicationId "org.telegram.messenger.beta"/applicationId "com.spnetgram.app.beta"/g' "$f"
done

log "Rebranding complete"

# ─── Copy SP NET GRAM custom source files ────────────────────────────────────
step "6/8 — Installing SP NET GRAM custom features"

DEST_JAVA="$OUTPUT_DIR/TMessagesProj/src/main/java/$NEW_PACKAGE_PATH"
SRC_JAVA="$SP_GRAM_DIR/app/src/main/java/com/spnetgram/app"

log "Copying AI features"
mkdir -p "$DEST_JAVA/ai"
cp -r "$SRC_JAVA/ai/." "$DEST_JAVA/ai/"

log "Copying premium system (SP Diamond + SP Coins)"
mkdir -p "$DEST_JAVA/premium"
cp -r "$SRC_JAVA/premium/." "$DEST_JAVA/premium/"

log "Copying theme engine"
mkdir -p "$DEST_JAVA/theme"
cp -r "$SRC_JAVA/theme/." "$DEST_JAVA/theme/"

log "Copying analytics manager"
mkdir -p "$DEST_JAVA/analytics"
cp -r "$SRC_JAVA/analytics/." "$DEST_JAVA/analytics/"

log "Copying security manager"
mkdir -p "$DEST_JAVA/security"
cp -r "$SRC_JAVA/security/." "$DEST_JAVA/security/"

log "Copying network layer"
mkdir -p "$DEST_JAVA/network"
cp -r "$SRC_JAVA/network/." "$DEST_JAVA/network/"

log "Copying UI additions"
mkdir -p "$DEST_JAVA/ui/premium"
cp -r "$SRC_JAVA/ui/premium/." "$DEST_JAVA/ui/premium/"
cp -r "$SRC_JAVA/ui/settings/AISettingsActivity.java" "$DEST_JAVA/ui/settings/" 2>/dev/null || true

# Copy SPNetGramApp.java to root package
cp "$SRC_JAVA/SPNetGramApp.java" "$DEST_JAVA/"

log "Copy SP NET GRAM feature files complete"

# ─── Merge dependencies into build.gradle ────────────────────────────────────
step "7/8 — Merging dependencies"

MAIN_BUILD="$OUTPUT_DIR/TMessagesProj/build.gradle"
if [ -f "$MAIN_BUILD" ]; then
    # Append SP NET GRAM dependencies if not already present
    if ! grep -q "billingclient" "$MAIN_BUILD"; then
        cat >> "$MAIN_BUILD" << 'EOF'

// ── SP NET GRAM additions ──────────────────────────────────────────────────
dependencies {
    implementation platform('com.google.firebase:firebase-bom:33.1.2')
    implementation 'com.google.firebase:firebase-analytics-ktx'
    implementation 'com.google.firebase:firebase-crashlytics-ktx'
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.android.billingclient:billing-ktx:7.0.0'
    implementation 'com.squareup.retrofit2:retrofit:2.11.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    implementation 'androidx.biometric:biometric:1.2.0-alpha05'
    implementation 'com.airbnb.android:lottie:6.4.1'
    implementation 'jp.wasabeef:blurry:4.0.1'
    implementation 'io.coil-kt:coil:2.7.0'
}
EOF
        log "Dependencies merged"
    else
        log "Dependencies already present — skipping"
    fi
fi

# Add Google Services plugin to root build.gradle
ROOT_BUILD="$OUTPUT_DIR/build.gradle"
if [ -f "$ROOT_BUILD" ] && ! grep -q "google-services" "$ROOT_BUILD"; then
    sed -i "s|dependencies {|dependencies {\n        classpath 'com.google.gms:google-services:4.4.2'\n        classpath 'com.google.firebase:firebase-crashlytics-gradle:3.0.2'|" "$ROOT_BUILD"
    log "Google Services plugin added"
fi

# ─── Final verification ───────────────────────────────────────────────────────
step "8/8 — Verification"

log "SP NET GRAM project created at: $OUTPUT_DIR"
echo ""
echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  SP NET GRAM setup complete!${NC}"
echo -e "${GREEN}════════════════════════════════════════════════════════════${NC}"
echo ""
echo "  Project location:  $OUTPUT_DIR"
echo ""
echo "  Next steps:"
echo "  1. Open $OUTPUT_DIR in Android Studio"
echo "  2. Add your google-services.json to TMessagesProj/"
echo "  3. Add your Telegram API_ID and API_HASH to gradle.properties"
echo "  4. Run: ./gradlew assembleRelease"
echo ""
echo "  See BUILD.md for full instructions."
echo ""
