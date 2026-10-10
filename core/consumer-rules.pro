# Aturan consumer :core -- otomatis ikut dipakai R8 saat :app di-build (release).

# Model response Gson. Nama field dibaca lewat @SerializedName, tetapi R8 tetap boleh
# menghapus konstruktor/field yang "tidak terpakai", jadi seluruh paket response dipertahankan.
-keep class com.mahfudl.movieverse.core.data.remote.response.** { *; }

# SQLCipher (enkripsi database): class diakses dari kode native lewat JNI.
-keep class net.zetetic.database.** { *; }
-keepclassmembers class net.zetetic.database.** { native <methods>; }
-dontwarn net.zetetic.database.**
