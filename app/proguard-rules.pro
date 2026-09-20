# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for Play Console stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve all JNI native methods across the entire app and libraries
-keepclasseswithmembernames class * {
    native <methods>;
}

# MPV core and JNI bindings (do not allow optimization to alter JNI signatures)
-keep class is.xyz.mpv.** { *; }
-keepclassmembers class is.xyz.mpv.** { *; }
-keep class net.mediaarea.mediainfo.lib.** { *; }
-keepclassmembers class net.mediaarea.mediainfo.lib.** { *; }
-keep class org.libtorrent4j.swig.libtorrent_jni { *; }
-keep class org.libtorrent4j.** { *; }

# Allow R8 to expand member access for more aggressive inlining and devirtualization
-allowaccessmodification

# AndroidX Media3 & Jellyfin FFmpeg Audio Decoder
-dontwarn androidx.media3.**
-keep class org.jellyfin.media3.decoder.ffmpeg.** { *; }
-keepclassmembers class org.jellyfin.media3.decoder.ffmpeg.** { *; }
-dontwarn org.jellyfin.media3.decoder.ffmpeg.**

# Room Database Entities and DAOs
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class app.infinity.mpvz.database.entities.** { *; }
-keep class app.infinity.mpvz.database.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Koin Dependency Injection
-dontwarn org.koin.**

# NanoHTTPD (local streaming & torrent proxy)
-keep class fi.iki.elonen.** { *; }
-dontwarn fi.iki.elonen.**

-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# SMBJ ProGuard Rules
# Keep SMBJ classes
-keep class com.hierynomus.smbj.** { *; }
-keep class com.hierynomus.mssmb2.** { *; }
-keep class com.hierynomus.msdtyp.** { *; }
-keep class com.hierynomus.msfscc.** { *; }
-keep class com.hierynomus.protocol.** { *; }
-keep class com.hierynomus.spnego.** { *; }
-keep class com.hierynomus.ntlm.** { *; }
-keep class com.hierynomus.security.** { *; }

# JGSS (Kerberos/SPNEGO) - Optional, not needed for basic NTLM auth
# These are used for domain authentication, which we don't use
-dontwarn org.ietf.jgss.**
-dontnote org.ietf.jgss.**

# MBassador (event bus used by SMBJ) - EL (Expression Language) is optional
-dontwarn net.engio.mbassy.dispatch.el.**
-dontnote net.engio.mbassy.dispatch.el.**
-dontwarn javax.el.**
-dontnote javax.el.**

# Keep MBassador core classes
-keep class net.engio.mbassy.** { *; }

# BouncyCastle (crypto provider used by SMBJ)
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# JSch (SFTP) resolves cipher/KEX/signature implementations reflectively from config strings
-keep class com.jcraft.jsch.** { *; }
-dontwarn com.jcraft.jsch.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn org.newsclub.net.unix.**

# ASN.1 classes
-keep class com.hierynomus.asn1.** { *; }

# Keep all classes that use reflection or are loaded dynamically
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-keep class com.google.android.gms.common.internal.safeparcel.SafeParcelable { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.gms.ads.identifier.**

# Kotlinx Serialization
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep @kotlinx.serialization.Serializable class * { *; }

# Retrofit & OkHttp (Stream Catalog)
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
