package com.mahfudl.movieverse.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.model.mapData
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import com.mahfudl.movieverse.ui.model.MovieUi
import com.mahfudl.movieverse.ui.model.toUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SearchViewModel(useCase: MovieUseCase) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refresh = MutableStateFlow(0)

    /** null = belum ada pencarian (kondisi idle). */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val state: StateFlow<Resource<List<MovieUi>>?> = combine(
        query.debounce(500).map { it.trim() }.distinctUntilChanged(),
        refresh
    ) { q, _ -> q }
        .flatMapLatest<String, Resource<List<MovieUi>>?> { q ->
            if (q.length < 2) {
                flowOf(null)
            } else {
                useCase.searchMovies(q).map { res -> res.mapData { list -> list.map { it.toUi() } } }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setQuery(value: String) {
        query.value = value
    }

    fun retry() = refresh.update { it + 1 }
}
