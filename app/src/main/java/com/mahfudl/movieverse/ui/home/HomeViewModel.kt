package com.mahfudl.movieverse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.model.mapData
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import com.mahfudl.movieverse.ui.model.MovieUi
import com.mahfudl.movieverse.ui.model.toUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel(useCase: MovieUseCase) : ViewModel() {

    private val category = MutableStateFlow(MovieCategory.POPULAR)
    private val refresh = MutableStateFlow(0)

    val selectedCategory: StateFlow<MovieCategory> = category

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<Resource<List<MovieUi>>> = combine(category, refresh) { c, _ -> c }
        .flatMapLatest { useCase.getMovies(it) }
        .map { res -> res.mapData { list -> list.map { it.toUi() } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading)

    fun setCategory(value: MovieCategory) {
        category.value = value
    }

    fun retry() = refresh.update { it + 1 }
}
