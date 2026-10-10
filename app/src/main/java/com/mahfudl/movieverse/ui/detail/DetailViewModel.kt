package com.mahfudl.movieverse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.model.mapData
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import com.mahfudl.movieverse.ui.model.MovieUi
import com.mahfudl.movieverse.ui.model.toUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(private val movieId: Int, private val useCase: MovieUseCase) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private var currentMovie: Movie? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<Resource<MovieUi>> = retryTrigger
        .flatMapLatest { useCase.getMovieDetail(movieId) }
        .onEach { if (it is Resource.Success) currentMovie = it.data }
        .map { res -> res.mapData { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Resource.Loading)

    val isFavorite: StateFlow<Boolean> = useCase.isFavorite(movieId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleFavorite() {
        val movie = currentMovie ?: return
        viewModelScope.launch {
            if (isFavorite.value) useCase.removeFavorite(movie.movieId) else useCase.addFavorite(movie)
        }
    }

    fun retry() = retryTrigger.update { it + 1 }
}
