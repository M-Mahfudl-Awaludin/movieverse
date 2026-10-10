package com.mahfudl.movieverse.core.domain.usecase

import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.repository.IMovieRepository
import kotlinx.coroutines.flow.Flow

interface MovieUseCase {
    fun getMovies(category: MovieCategory): Flow<Resource<List<Movie>>>
    fun searchMovies(query: String): Flow<Resource<List<Movie>>>
    fun getMovieDetail(movieId: Int): Flow<Resource<Movie>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun isFavorite(movieId: Int): Flow<Boolean>
    suspend fun addFavorite(movie: Movie)
    suspend fun removeFavorite(movieId: Int)
}

class MovieInteractor(private val repository: IMovieRepository) : MovieUseCase {
    override fun getMovies(category: MovieCategory) = repository.getMovies(category)
    override fun searchMovies(query: String) = repository.searchMovies(query)
    override fun getMovieDetail(movieId: Int) = repository.getMovieDetail(movieId)
    override fun getFavoriteMovies() = repository.getFavoriteMovies()
    override fun isFavorite(movieId: Int) = repository.isFavorite(movieId)
    override suspend fun addFavorite(movie: Movie) = repository.addFavorite(movie)
    override suspend fun removeFavorite(movieId: Int) = repository.removeFavorite(movieId)
}
