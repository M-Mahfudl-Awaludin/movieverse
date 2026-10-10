package com.mahfudl.movieverse.core.data.repository

import app.cash.turbine.test
import com.mahfudl.movieverse.core.data.local.LocalDataSource
import com.mahfudl.movieverse.core.data.local.MovieDao
import com.mahfudl.movieverse.core.data.local.MovieEntity
import com.mahfudl.movieverse.core.data.remote.ApiService
import com.mahfudl.movieverse.core.data.remote.RemoteDataSource
import com.mahfudl.movieverse.core.data.remote.response.MovieDto
import com.mahfudl.movieverse.core.data.remote.response.MovieListResponse
import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.testDto
import com.mahfudl.movieverse.core.testEntity
import com.mahfudl.movieverse.core.testMovie
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRepositoryTest {

    private val api = FakeApiService()
    private val dao = FakeMovieDao()
    private val repository = MovieRepository(RemoteDataSource(api), LocalDataSource(dao))

    @Test
    fun getMoviesEmitsLoadingThenMappedSuccess() = runTest {
        api.list = MovieListResponse(listOf(testDto(1), testDto(null), testDto(2), testDto(2)))

        val result = repository.getMovies(MovieCategory.POPULAR).toList()

        assertEquals(2, result.size)
        assertEquals(Resource.Loading, result[0])
        val success = result[1] as Resource.Success<List<Movie>>
        assertEquals(listOf(1, 2), success.data.map { it.movieId })
    }

    @Test
    fun getMoviesCallsEndpointThatMatchesCategory() = runTest {
        val expected = mapOf(
            MovieCategory.POPULAR to "popular",
            MovieCategory.TOP_RATED to "top_rated",
            MovieCategory.NOW_PLAYING to "now_playing",
            MovieCategory.UPCOMING to "upcoming"
        )

        expected.forEach { (category, endpoint) ->
            api.calls.clear()
            repository.getMovies(category).toList()
            assertEquals(listOf(endpoint), api.calls)
        }
    }

    @Test
    fun getMoviesTurnsNullResultsIntoEmptySuccess() = runTest {
        api.list = MovieListResponse(null)

        val result = repository.getMovies(MovieCategory.POPULAR).toList()

        assertEquals(Resource.Success(emptyList<Movie>()), result.last())
    }

    @Test
    fun getMoviesEmitsErrorInsteadOfThrowing() = runTest {
        api.failure = IOException("offline")

        val result = repository.getMovies(MovieCategory.POPULAR).toList()

        assertEquals(2, result.size)
        assertEquals(Resource.Loading, result[0])
        val error = result[1] as Resource.Error
        assertTrue(error.message.contains("internet"))
    }

    @Test
    fun searchMoviesForwardsTheQuery() = runTest {
        api.list = MovieListResponse(listOf(testDto(7)))

        val result = repository.searchMovies("batman").toList()

        assertEquals("batman", api.lastQuery)
        assertTrue(result.last() is Resource.Success<*>)
    }

    @Test
    fun searchMoviesEmitsErrorOnFailure() = runTest {
        api.failure = IllegalStateException("bad")

        val result = repository.searchMovies("batman").toList()

        assertEquals(Resource.Error("bad"), result.last())
    }

    @Test
    fun getMovieDetailEmitsLoadingThenSuccess() = runTest {
        api.detail = testDto(id = 5, title = "Remote")

        val result = repository.getMovieDetail(5).toList()

        assertEquals(Resource.Loading, result[0])
        val success = result[1] as Resource.Success<Movie>
        assertEquals("Remote", success.data.title)
    }

    @Test
    fun getMovieDetailFallsBackToCachedFavoriteWhenOffline() = runTest {
        dao.rows.value = mapOf(5 to testEntity(5))
        api.failure = IOException()

        val result = repository.getMovieDetail(5).toList()

        val success = result.last() as Resource.Success<Movie>
        assertEquals(5, success.data.movieId)
    }

    @Test
    fun getMovieDetailEmitsErrorWhenOfflineAndNotCached() = runTest {
        api.failure = IOException()

        val result = repository.getMovieDetail(5).toList()

        assertTrue(result.last() is Resource.Error)
    }

    @Test
    fun getMovieDetailSurvivesBrokenDatabase() = runTest {
        api.failure = IOException()
        dao.failReads = true

        val result = repository.getMovieDetail(5).toList()

        assertTrue(result.last() is Resource.Error)
    }

    @Test
    fun getMovieDetailWithoutIdIsAnError() = runTest {
        api.detail = testDto(id = null)

        val result = repository.getMovieDetail(5).toList()

        assertEquals(Resource.Error("Terjadi kesalahan tak terduga."), result.last())
    }

    @Test
    fun favoritesFlowReflectsAddAndRemove() = runTest {
        repository.getFavoriteMovies().test {
            assertTrue(awaitItem().isEmpty())

            repository.addFavorite(testMovie(1, favoritedAt = 5_000L))
            val added = awaitItem()
            assertEquals(listOf(1), added.map { it.movieId })
            assertEquals(5_000L, added.single().favoritedAt)

            repository.removeFavorite(1)
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavoriteFollowsDatabase() = runTest {
        assertFalse(repository.isFavorite(1).first())

        repository.addFavorite(testMovie(1))

        assertTrue(repository.isFavorite(1).first())
    }

    @Test
    fun brokenDatabaseReadsDoNotCrash() = runTest {
        dao.failReads = true

        assertTrue(repository.getFavoriteMovies().first().isEmpty())
        assertFalse(repository.isFavorite(1).first())
    }

    @Test
    fun brokenDatabaseWritesDoNotCrash() = runTest {
        dao.failWrites = true

        repository.addFavorite(testMovie(1))
        repository.removeFavorite(1)

        assertTrue(dao.rows.value.isEmpty())
    }
}

private class FakeApiService : ApiService {
    var list = MovieListResponse(emptyList())
    var detail: MovieDto = testDto()
    var failure: Throwable? = null
    var lastQuery: String? = null
    val calls = mutableListOf<String>()

    private fun respond(endpoint: String): MovieListResponse {
        calls += endpoint
        failure?.let { throw it }
        return list
    }

    override suspend fun getPopular(page: Int) = respond("popular")

    override suspend fun getTopRated(page: Int) = respond("top_rated")

    override suspend fun getNowPlaying(page: Int) = respond("now_playing")

    override suspend fun getUpcoming(page: Int) = respond("upcoming")

    override suspend fun searchMovies(query: String, includeAdult: Boolean, page: Int): MovieListResponse {
        lastQuery = query
        return respond("search")
    }

    override suspend fun getMovieDetail(id: Int): MovieDto {
        failure?.let { throw it }
        return detail
    }
}

private class FakeMovieDao : MovieDao {
    val rows = MutableStateFlow<Map<Int, MovieEntity>>(emptyMap())
    var failReads = false
    var failWrites = false

    override fun getAll(): Flow<List<MovieEntity>> = if (failReads) {
        flow<List<MovieEntity>> { error("db corrupt") }
    } else {
        rows.map { map -> map.values.sortedByDescending { it.addedAt } }
    }

    override suspend fun getById(movieId: Int): MovieEntity? {
        check(!failReads) { "db corrupt" }
        return rows.value[movieId]
    }

    override fun isFavorite(movieId: Int): Flow<Boolean> = if (failReads) {
        flow<Boolean> { error("db corrupt") }
    } else {
        rows.map { it.containsKey(movieId) }
    }

    override suspend fun insert(entity: MovieEntity) {
        check(!failWrites) { "db locked" }
        rows.update { it + (entity.movieId to entity) }
    }

    override suspend fun delete(movieId: Int) {
        check(!failWrites) { "db locked" }
        rows.update { it - movieId }
    }
}
