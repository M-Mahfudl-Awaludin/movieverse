package com.mahfudl.movieverse.core.domain

import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.repository.IMovieRepository
import com.mahfudl.movieverse.core.domain.usecase.MovieInteractor
import com.mahfudl.movieverse.core.testMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class MovieInteractorTest {

    private val repository = RecordingRepository()
    private val interactor = MovieInteractor(repository)

    @Test
    fun getMoviesDelegatesToRepository() {
        val result = interactor.getMovies(MovieCategory.TOP_RATED)

        assertSame(repository.movies, result)
        assertEquals(listOf("getMovies:TOP_RATED"), repository.log)
    }

    @Test
    fun searchMoviesDelegatesToRepository() {
        val result = interactor.searchMovies("matrix")

        assertSame(repository.movies, result)
        assertEquals(listOf("searchMovies:matrix"), repository.log)
    }

    @Test
    fun getMovieDetailDelegatesToRepository() {
        val result = interactor.getMovieDetail(42)

        assertSame(repository.detail, result)
        assertEquals(listOf("getMovieDetail:42"), repository.log)
    }

    @Test
    fun favoriteReadsDelegateToRepository() {
        assertSame(repository.favorites, interactor.getFavoriteMovies())
        assertSame(repository.favoriteFlag, interactor.isFavorite(3))
        assertEquals(listOf("getFavoriteMovies", "isFavorite:3"), repository.log)
    }

    @Test
    fun favoriteWritesDelegateToRepository() = runTest {
        interactor.addFavorite(testMovie(8))
        interactor.removeFavorite(8)

        assertEquals(listOf("addFavorite:8", "removeFavorite:8"), repository.log)
    }
}

private class RecordingRepository : IMovieRepository {
    val log = mutableListOf<String>()
    val movies: Flow<Resource<List<Movie>>> = flowOf(Resource.Success(emptyList()))
    val detail: Flow<Resource<Movie>> = flowOf(Resource.Loading)
    val favorites: Flow<List<Movie>> = flowOf(emptyList())
    val favoriteFlag: Flow<Boolean> = flowOf(false)

    override fun getMovies(category: MovieCategory): Flow<Resource<List<Movie>>> {
        log += "getMovies:$category"
        return movies
    }

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> {
        log += "searchMovies:$query"
        return movies
    }

    override fun getMovieDetail(movieId: Int): Flow<Resource<Movie>> {
        log += "getMovieDetail:$movieId"
        return detail
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        log += "getFavoriteMovies"
        return favorites
    }

    override fun isFavorite(movieId: Int): Flow<Boolean> {
        log += "isFavorite:$movieId"
        return favoriteFlag
    }

    override suspend fun addFavorite(movie: Movie) {
        log += "addFavorite:${movie.movieId}"
    }

    override suspend fun removeFavorite(movieId: Int) {
        log += "removeFavorite:$movieId"
    }
}
