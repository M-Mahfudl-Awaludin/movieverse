# =====================================================================
# ProGuard / R8 untuk module :core (library) -- aktif di build debug & release.
#
# Aturan di berkas ini hanya dipakai saat :core diproses sendiri sebagai library.
# Obfuscation penuh seluruh class :core (termasuk security/DatabaseFactory, CertificatePins,
# DatabasePassphraseProvider) terjadi di APK, ketika R8 module :app memproses :core
# bersama aturan dari consumer-rules.pro dan app/proguard-rules.pro.
#
# Sebagai library, API publiknya dipakai module lain lewat nama, jadi di sini nama
# class :core dipertahankan agar module :app dan :favorite tetap bisa dikompilasi dan berjalan.
# =====================================================================

-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

-keep class com.mahfudl.movieverse.core.** { *; }

# Model Gson / interface Retrofit
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp / SQLCipher
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**
