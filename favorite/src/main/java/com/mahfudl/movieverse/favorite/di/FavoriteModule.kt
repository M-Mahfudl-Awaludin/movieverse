package com.mahfudl.movieverse.favorite.di

import com.mahfudl.movieverse.favorite.FavoriteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

val favoriteModule = module {
    viewModel { FavoriteViewModel(get()) }
}

private val loadFavoriteKoin by lazy { loadKoinModules(favoriteModule) }

/** Dipanggil saat fitur dinamis dibuka; module Koin dimuat tepat satu kali. */
fun injectFavoriteFeature() = loadFavoriteKoin
