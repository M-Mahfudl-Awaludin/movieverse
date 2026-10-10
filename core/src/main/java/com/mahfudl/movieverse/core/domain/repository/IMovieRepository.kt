package com.mahfudl.movieverse.core.domain.repository

import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface IMovieRepository {
    fun getMovies(category: MovieCategory): Flow<Resource<List<Movie>>>
    fun searchMovies(query: String): Flow<Resource<List<Movie>>>
    fun getMovieDetail(movieId: Int): Flow<Resource<Movie>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun isFavorite(movieId: Int): Flow<Boolean>
    suspend fun addFavorite(movie: Movie)
    suspend fun removeFavorite(movieId: Int)
}
