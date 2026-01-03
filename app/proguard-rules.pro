# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ============================================
# HABITATE PROGUARD RULES
# ============================================

# ============================================
# KOTLIN SERIALIZATION
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.ninety5.habitate.**$$serializer { *; }
-keepclassmembers class com.ninety5.habitate.** {
    *** Companion;
}
-keepclasseswithmembers class com.ninety5.habitate.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# MOSHI
# ============================================
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep @com.squareup.moshi.JsonQualifier interface *

# Keep generated Moshi adapters
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}

# ============================================
# RETROFIT & OKHTTP
# ============================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================
# ROOM DATABASE
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================
# FIREBASE
# ============================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ============================================
# HILT / DAGGER
# ============================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

# ============================================
# DATA CLASSES & MODELS
# ============================================
-keep class com.ninety5.habitate.data.model.** { *; }
-keep class com.ninety5.habitate.data.local.entity.** { *; }
-keep class com.ninety5.habitate.data.remote.dto.** { *; }
-keep class com.ninety5.habitate.data.remote.WsMessage { *; }
-keep class com.ninety5.habitate.data.remote.WsMessage$* { *; }

# ============================================
# COMPOSE
# ============================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================
# TIMBER LOGGING (strip in release)
# ============================================
-assumenosideeffects class timber.log.Timber* {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# ============================================
# NOTHING GLYPH SDK
# ============================================
-keep class com.nothing.ketchum.** { *; }
-dontwarn com.nothing.ketchum.**

# ============================================
# GENERAL ANDROID
# ============================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Keep View binding classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ============================================
# COROUTINES
# ============================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
