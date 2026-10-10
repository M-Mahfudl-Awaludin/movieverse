package com.mahfudl.movieverse.di

import com.mahfudl.movieverse.core.domain.usecase.MovieInteractor
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import com.mahfudl.movieverse.core.domain.usecase.SettingInteractor
import com.mahfudl.movieverse.core.domain.usecase.SettingUseCase
import com.mahfudl.movieverse.ui.detail.DetailViewModel
import com.mahfudl.movieverse.ui.home.HomeViewModel
import com.mahfudl.movieverse.ui.search.SearchViewModel
import com.mahfudl.movieverse.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory<MovieUseCase> { MovieInteractor(get()) }
    factory<SettingUseCase> { SettingInteractor(get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { (movieId: Int) -> DetailViewModel(movieId, get()) }
    viewModel { SettingsViewModel(get()) }
}
