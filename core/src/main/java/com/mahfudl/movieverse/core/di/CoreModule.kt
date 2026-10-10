package com.mahfudl.movieverse.core.di

import com.mahfudl.movieverse.core.BuildConfig
import com.mahfudl.movieverse.core.data.local.DatabaseFactory
import com.mahfudl.movieverse.core.data.local.LocalDataSource
import com.mahfudl.movieverse.core.data.local.MovieDatabase
import com.mahfudl.movieverse.core.data.local.SettingPreferences
import com.mahfudl.movieverse.core.data.remote.ApiService
import com.mahfudl.movieverse.core.data.remote.RemoteDataSource
import com.mahfudl.movieverse.core.data.repository.MovieRepository
import com.mahfudl.movieverse.core.data.repository.SettingRepository
import com.mahfudl.movieverse.core.domain.repository.IMovieRepository
import com.mahfudl.movieverse.core.domain.repository.ISettingRepository
import com.mahfudl.movieverse.core.security.CertificatePins
import com.mahfudl.movieverse.core.security.DatabasePassphraseProvider
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.themoviedb.org/3/"
private const val TIMEOUT_SECONDS = 30L

val databaseModule = module {
    single { DatabasePassphraseProvider(androidContext()) }
    // Database terenkripsi SQLCipher, passphrase dilindungi Android Keystore
    single { DatabaseFactory.create(androidContext(), get()) }
    factory { get<MovieDatabase>().movieDao() }
}

val networkModule = module {
    single {
        // Menambahkan api_key & bahasa ke setiap request TMDB
        val apiKeyInterceptor = Interceptor { chain ->
            val url = chain.request().url.newBuilder()
                .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                .addQueryParameter("language", "en-US")
                .build()
            chain.proceed(chain.request().newBuilder().url(url).build())
        }
        val logging = HttpLoggingInterceptor().setLevel(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        )
        OkHttpClient.Builder()
            // Certificate pinning: hanya menerima rantai sertifikat TMDB yang sesuai pin
            .certificatePinner(CertificatePins.pinner())
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
            .create(ApiService::class.java)
    }
}

val repositoryModule = module {
    single { RemoteDataSource(get()) }
    single { LocalDataSource(get()) }
    single { SettingPreferences(androidContext()) }
    single<IMovieRepository> { MovieRepository(get(), get()) }
    single<ISettingRepository> { SettingRepository(get()) }
}

/** Kumpulan module DI milik :core, dimuat oleh :app. */
val coreModules = listOf(databaseModule, networkModule, repositoryModule)
