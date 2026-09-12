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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontobfuscate

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

# AndroidX Media3 & Jellyfin FFmpeg Audio Decoder
-keep class androidx.media3.** { *; }
-keepclassmembers class androidx.media3.** { *; }
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
-keep class org.koin.** { *; }
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

# Google Mobile Ads (AdMob) - keep the reflection-based internal APIs
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.common.internal.safeparcel.SafeParcelable { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.gms.ads.identifier.**

