package com.mahfudl.movieverse

import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Implementasi [MovieUseCase] buatan tangan untuk test ViewModel (tanpa library mocking). */
class FakeMovieUseCase : MovieUseCase {

    val moviesByCategory = mutableMapOf<MovieCategory, Resource<List<Movie>>>()
    val requestedCategories = mutableListOf<MovieCategory>()

    var searchResult: Resource<List<Movie>> = Resource.Success(emptyList())
    val searchedQueries = mutableListOf<String>()

    var detail: Resource<Movie> = Resource.Loading
    var detailRequests = 0

    val favorites = MutableStateFlow<List<Movie>>(emptyList())
    val added = mutableListOf<Movie>()
    val removed = mutableListOf<Int>()

    override fun getMovies(category: MovieCategory): Flow<Resource<List<Movie>>> = flow<Resource<List<Movie>>> {
        requestedCategories += category
        emit(moviesByCategory[category] ?: Resource.Success(emptyList()))
    }

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> = flow<Resource<List<Movie>>> {
        searchedQueries += query
        emit(searchResult)
    }

    override fun getMovieDetail(movieId: Int): Flow<Resource<Movie>> = flow<Resource<Movie>> {
        detailRequests++
        emit(detail)
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> = favorites

    override fun isFavorite(movieId: Int): Flow<Boolean> = favorites.map { list -> list.any { it.movieId == movieId } }

    override suspend fun addFavorite(movie: Movie) {
        added += movie
        favorites.update { it + movie }
    }

    override suspend fun removeFavorite(movieId: Int) {
        removed += movieId
        favorites.update { list -> list.filterNot { it.movieId == movieId } }
    }
}
