# SP NET GRAM ProGuard Rules

# Keep application class
-keep class com.spnetgram.app.SPNetGramApp { *; }

# Keep all model/data classes
-keep class com.spnetgram.app.utils.AccountManager$Account { *; }
-keep class com.spnetgram.app.theme.ThemeEngine$ThemeConfig { *; }
-keep class com.spnetgram.app.premium.** { *; }

# Keep AI callbacks
-keep interface com.spnetgram.app.ai.AIManager$AICallback { *; }
-keep interface com.spnetgram.app.ai.AIVoiceNoteHelper$VoiceNoteCallback { *; }

# Keep analytics
-keep class com.spnetgram.app.analytics.AnalyticsManager { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Billing
-keep class com.android.billingclient.api.** { *; }

# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Telegram MTProto layer — preserve all TL schema classes
-keep class org.telegram.tgnet.** { *; }
-keep class org.telegram.messenger.** { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
