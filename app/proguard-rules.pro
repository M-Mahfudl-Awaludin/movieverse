# =====================================================================
# ProGuard / R8 -- obfuscation, shrinking & optimization (build debug & release)
# Diaktifkan lewat isMinifyEnabled = true pada app/build.gradle.kts.
# Catatan: aturan untuk modul dynamic feature (:favorite) cukup ditulis di sini,
# karena R8 memproses :app, :core, dan :favorite dalam satu kali proses.
# =====================================================================

# ---- Stack trace tetap bisa dibaca (gunakan mapping.txt untuk de-obfuscate) ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Obfuscation agresif: nama paket/kelas/method diacak ----
-repackageclasses 'mv'
-allowaccessmodification

# ---- Atribut yang dibutuhkan Gson / Retrofit (generic type, annotation) ----
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# ---- Model response Gson (lihat juga core/consumer-rules.pro) ----
-keep class com.mahfudl.movieverse.core.data.remote.response.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---- Retrofit ----
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ---- OkHttp / certificate pinning ----
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- SQLCipher (database terenkripsi) ----
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# ---- Fragment dibuat lewat nama kelas oleh Navigation (termasuk dynamic feature) ----
-keep class * extends androidx.fragment.app.Fragment { <init>(); }

# ---- Glide ----
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ---- Hapus log di build release ----
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ---- invokedynamic string concat (Kotlin jvmTarget 17) di-desugar oleh D8/R8 ----
-dontwarn java.lang.invoke.StringConcatFactory
