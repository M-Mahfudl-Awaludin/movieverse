package com.mahfudl.movieverse.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import com.mahfudl.movieverse.ui.model.MovieUi
import com.mahfudl.movieverse.ui.model.toUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(private val useCase: MovieUseCase) : ViewModel() {

    private var cache: List<Movie> = emptyList()
    private var lastRemoved: Movie? = null

    /** null = data belum dimuat dari database. */
    val favorites: StateFlow<List<MovieUi>?> = useCase.getFavoriteMovies()
        .onEach { cache = it }
        .map { list -> list.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun remove(movieId: Int) {
        val target = cache.firstOrNull { it.movieId == movieId } ?: return
        lastRemoved = target
        viewModelScope.launch { useCase.removeFavorite(movieId) }
    }

    fun undoRemove() {
        val movie = lastRemoved ?: return
        lastRemoved = null
        viewModelScope.launch { useCase.addFavorite(movie) }
    }
}
