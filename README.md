# MovieVerse – Submission Capstone Akhir (Dicoding)

Aplikasi katalog film memakai **TheMovieDB (TMDB) API v3**.

[![CI](https://github.com/<USERNAME>/<REPO>/actions/workflows/ci.yml/badge.svg)](https://github.com/<USERNAME>/<REPO>/actions/workflows/ci.yml)

> Ganti `<USERNAME>/<REPO>` dengan repository GitHub Anda. **Tautan CI untuk catatan reviewer** adalah
> tautan *run* CI terakhir yang berstatus hijau, bukan tautan repository:
> `https://github.com/<USERNAME>/<REPO>/actions/runs/<ID_RUN>`

## API key
Urutan pencarian: environment variable `TMDB_API_KEY` → `local.properties` → `gradle.properties`.
Nilainya dimasukkan ke `BuildConfig` module `:core`. API key gratis: https://www.themoviedb.org/settings/api

## Arsitektur
- `:app`      – Presentation (Fragment XML, ViewModel, Navigation, model `MovieUi`)
- `:core`     – Android Library: `domain` (model, repository interface, use case) + `data` (Retrofit, Room, DataStore, repository impl) + DI
- `:favorite` – Dynamic Feature (install-time) untuk daftar favorit

Dependency rule: presentation -> domain <- data. Tiga model terpisah:
`MovieDto`/`MovieEntity` (data) -> `Movie` (domain) -> `MovieUi` (presentation).

Teknologi: Kotlin, Koin, Kotlin Flow/StateFlow, Retrofit + OkHttp, Room, DataStore, Navigation (+ dynamic features), Material 3, Glide, Shimmer.

Fitur: Populer / Terbaik / Sedang Tayang / Mendatang, Detail (mode offline untuk film favorit), Favorit (geser untuk hapus + urungkan), Pencarian (debounce 500 ms), Pengaturan tema gelap.

## Catatan untuk reviewer

### 1. Continuous Integration
GitHub Actions – `.github/workflows/ci.yml`. Satu run menjalankan:
ktlint (code style), Android Lint (Inspect Code), unit test 3 module, laporan code coverage (Jacoco),
`assembleDebug`, `assembleRelease` (R8), serta Trivy (vulnerability scan) dan pemeriksaan certificate pin.
CodeQL ada di `.github/workflows/codeql.yml`; Dependabot di `.github/dependabot.yml`.
Tautan CI: lihat bagian atas berkas ini.

### 2. Performa
- **LeakCanary** `2.14` – `debugImplementation` di `app/build.gradle.kts` (aktif otomatis pada build debug).
- Referensi View dilepas di `onDestroyView` (binding, adapter, `ViewPager2` callback, `TabLayoutMediator`, `ItemTouchHelper`).
- **StrictMode** (debug saja) di `MovieVerseApp.enableStrictModeInDebug()`.
- Perbaikan Inspect Code: `ObsoleteSdkInt` (folder `mipmap-anydpi-v26` -> `mipmap-anydpi`, minSdk 26), `ShrinkResources` (`isShrinkResources = true`), overdraw (background root dipindah ke `windowBackground`), `baselineAligned=false`,
  resource tidak terpakai (0), autofill, string i18n, tanpa alokasi objek di `onChildDraw`.

### 3. Keamanan
| Teknik | Lokasi class / berkas |
| --- | --- |
| **Obfuscation (R8/ProGuard)** | `isMinifyEnabled = true` pada build type **debug dan release** di `app/build.gradle.kts` dan `core/build.gradle.kts`; aturan di `app/proguard-rules.pro`, `core/proguard-rules.pro`, `core/consumer-rules.pro`. `isShrinkResources = true` di `:app` |
| **Enkripsi database (SQLCipher)** | `core/.../data/local/DatabaseFactory.kt` (`SupportOpenHelperFactory` untuk Room) |
| Passphrase database | `core/.../security/DatabasePassphraseProvider.kt` – 32 byte acak, dienkripsi AES-256/GCM dengan kunci **Android Keystore** |
| **Certificate pinning** | `core/.../security/CertificatePins.kt` (`CertificatePinner` OkHttp) dipasang di `core/.../di/CoreModule.kt` |
| Tambahan | `res/xml/network_security_config.xml` (cleartext dilarang), `allowBackup=false`, log HTTP hanya di debug, log `Log.v/d/i` dihapus di release |

Memperbarui pin: `scripts/update_pins.sh` (mengambil rantai sertifikat TMDB yang sebenarnya dan menulis ulang
`CertificatePins.kt`; `--check` hanya memverifikasi). Jalankan sebelum merilis dan saat sertifikat TMDB berganti.

### 3b. Pengujian
```
./gradlew :core:testDebugUnitTest :app:testDebugUnitTest :favorite:testDebugUnitTest
./gradlew :core:createDebugUnitTestCoverageReport :app:createDebugUnitTestCoverageReport :favorite:createDebugUnitTestCoverageReport
```
Laporan coverage: `<module>/build/reports/coverage/test/debug/index.html`.
Test memakai fake buatan tangan (tanpa library mocking): mapper, repository, use case, ViewModel (debounce
dengan virtual time), dan certificate pin. Kode yang bergantung pada Android Keystore tidak diuji unit.

### 4. Code style
`./ktlint "**/src/**/*.kt" "**/*.kts"` (ktlint 1.3.1, konfigurasi di `.editorconfig`).

This product uses the TMDB API but is not endorsed or certified by TMDB.
