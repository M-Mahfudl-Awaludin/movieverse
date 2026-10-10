package com.mahfudl.movieverse

import android.content.pm.ApplicationInfo
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.mahfudl.movieverse.core.di.coreModules
import com.mahfudl.movieverse.core.domain.usecase.SettingUseCase
import com.mahfudl.movieverse.di.useCaseModule
import com.mahfudl.movieverse.di.viewModelModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MovieVerseApp : SplitCompatApplication() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settingUseCase: SettingUseCase by inject()

    /** true setelah tema tersimpan selesai dibaca; dipakai splash screen agar tidak berkedip. */
    @Volatile
    var isThemeReady: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        enableStrictModeInDebug()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MovieVerseApp)
            modules(coreModules + useCaseModule + viewModelModule)
        }

        // Baca tema secara asinkron (tanpa memblokir main thread). Splash screen
        // ditahan di MainActivity sampai isThemeReady bernilai true.
        appScope.launch {
            val initial = withTimeoutOrNull(INITIAL_THEME_TIMEOUT_MS) { settingUseCase.getDarkMode().first() }
            applyNightMode(initial)
            isThemeReady = true
            settingUseCase.getDarkMode().collect { applyNightMode(it) }
        }
    }

    /**
     * Hanya saat debug: StrictMode mencatat (log) pekerjaan berat di main thread dan kebocoran
     * resource, sehingga masalah performa langsung terlihat di Logcat selama pengembangan.
     */
    private fun enableStrictModeInDebug() {
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0) return
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build()
        )
    }

    private fun applyNightMode(dark: Boolean?) {
        AppCompatDelegate.setDefaultNightMode(
            when (dark) {
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
                null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private companion object {
        const val INITIAL_THEME_TIMEOUT_MS = 1_500L
    }
}
