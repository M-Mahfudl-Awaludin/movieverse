plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mahfudl.movieverse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mahfudl.movieverse"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        debug {
            // Laporan code coverage: ./gradlew :app:createDebugUnitTestCoverageReport
            enableUnitTestCoverage = true
        }
        release {
            // Obfuscation + shrinking kode dengan R8/ProGuard (lihat proguard-rules.pro)
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // APK release ditandatangani debug key agar langsung bisa dipasang & diuji reviewer.
            // Untuk rilis sungguhan, ganti dengan signingConfig milik Anda.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = false
        checkDependencies = true
        htmlReport = true
        xmlReport = true
    }

    // Dynamic feature module
    dynamicFeatures += setOf(":favorite")
}

dependencies {
    implementation(project(":core"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.fragment:fragment-ktx:1.9.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // Jetpack Navigation + dynamic features
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    api("androidx.navigation:navigation-dynamic-features-fragment:2.7.7")
    implementation("com.google.android.play:feature-delivery:2.1.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Deteksi memory leak (hanya build debug; terpasang otomatis, tanpa kode tambahan)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    // Unit test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
}
