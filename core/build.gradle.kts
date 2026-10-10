import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// API key TMDB dicari berurutan: environment variable (CI) -> local.properties -> gradle.properties.
// Ganti dengan API key milik Anda (gratis di https://www.themoviedb.org/settings/api).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val tmdbApiKey: String = System.getenv("TMDB_API_KEY")?.takeIf { it.isNotBlank() }
    ?: localProperties.getProperty("TMDB_API_KEY")
    ?: (project.findProperty("TMDB_API_KEY") as String?)
    ?: ""

android {
    namespace = "com.mahfudl.movieverse.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
        // Aturan ProGuard/R8 yang otomatis ikut diterapkan pada modul yang memakai :core.
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures { buildConfig = true }

    buildTypes {
        debug {
            // Laporan code coverage: ./gradlew :core:createDebugUnitTestCoverageReport
            enableUnitTestCoverage = true
        }
        release {
            // Obfuscation dijalankan R8 pada modul :app (isMinifyEnabled = true di sana).
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = false
        htmlReport = true
        xmlReport = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")

    // Reactive (Kotlin Flow) + DI (Koin) dibagikan ke module lain
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    api("io.insert-koin:koin-android:3.5.6")

    // Network + certificate pinning (OkHttp CertificatePinner)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Database + enkripsi (SQLCipher)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:sqlcipher-android:4.19.1@aar")

    // Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Unit test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
}
