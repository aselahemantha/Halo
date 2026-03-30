# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name.
-renamesourcefileattribute SourceFile

# ==========================================
# Gson - used for alarm backup/restore
# ==========================================
# Prevent Gson from stripping fields during serialization/deserialization.
# Without these rules, exported JSON will have obfuscated keys and import will crash.
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson classes
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Keep domain model classes used with Gson (backup & restore)
-keep class com.exoticstech.halo.domain.model.Alarm { *; }
-keep class com.exoticstech.halo.domain.model.AlarmHistory { *; }

# Keep generic type info for Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ==========================================
# Room - database entities and DAOs
# ==========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==========================================
# Google Play Services - Location & Maps
# ==========================================
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.maps.** { *; }

# ==========================================
# Google Places API
# ==========================================
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**

# ==========================================
# ZXing - QR Code generation
# ==========================================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ==========================================
# Firebase
# ==========================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ==========================================
# Glance (App Widgets)
# ==========================================
-keep class androidx.glance.** { *; }

# ==========================================
# Hilt / Dagger
# ==========================================
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

# ==========================================
# Kotlin Coroutines
# ==========================================
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ==========================================
# Jetpack Compose
# ==========================================
-dontwarn androidx.compose.**

# ==========================================
# Lottie
# ==========================================
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }