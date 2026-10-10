package com.mahfudl.movieverse.favorite

import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.usecase.MovieUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val useCase = FakeFavoriteUseCase()

    private fun <T> TestScope.collectStates(flow: Flow<T>): List<T> {
        val states = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(states) }
        return states
    }

    @Test
    fun favoritesAreNullUntilLoaded() {
        val viewModel = FavoriteViewModel(useCase)

        assertNull(viewModel.favorites.value)
    }

    @Test
    fun favoritesAreMappedToUiModels() = runTest {
        useCase.favorites.value = listOf(movie(1, "Inception"), movie(2, "Parasite"))
        val viewModel = FavoriteViewModel(useCase)

        val states = collectStates(viewModel.favorites)

        assertEquals(listOf("Inception", "Parasite"), states.last()?.map { it.title })
    }

    @Test
    fun removeDeletesTheMovie() = runTest {
        useCase.favorites.value = listOf(movie(1, "Inception"))
        val viewModel = FavoriteViewModel(useCase)
        val states = collectStates(viewModel.favorites)

        viewModel.remove(1)

        assertEquals(listOf(1), useCase.removed)
        assertTrue(states.last().orEmpty().isEmpty())
    }

    @Test
    fun undoRestoresMovieWithItsOriginalFavoritedAt() = runTest {
        useCase.favorites.value = listOf(movie(1, "Inception", favoritedAt = 111L))
        val viewModel = FavoriteViewModel(useCase)
        val states = collectStates(viewModel.favorites)
        viewModel.remove(1)

        viewModel.undoRemove()

        assertEquals(111L, useCase.added.single().favoritedAt)
        assertEquals(listOf("Inception"), states.last()?.map { it.title })
    }

    @Test
    fun removeUnknownMovieDoesNothing() = runTest {
        useCase.favorites.value = listOf(movie(1, "Inception"))
        val viewModel = FavoriteViewModel(useCase)
        collectStates(viewModel.favorites)

        viewModel.remove(99)
        viewModel.undoRemove()

        assertTrue(useCase.removed.isEmpty())
        assertTrue(useCase.added.isEmpty())
    }

    @Test
    fun undoWithoutRemoveDoesNothing() = runTest {
        val viewModel = FavoriteViewModel(useCase)
        collectStates(viewModel.favorites)

        viewModel.undoRemove()

        assertTrue(useCase.added.isEmpty())
    }

    @Test
    fun undoOnlyRestoresOnce() = runTest {
        useCase.favorites.value = listOf(movie(1, "Inception"))
        val viewModel = FavoriteViewModel(useCase)
        collectStates(viewModel.favorites)
        viewModel.remove(1)

        viewModel.undoRemove()
        viewModel.undoRemove()

        assertEquals(1, useCase.added.size)
    }

    private fun movie(id: Int, title: String, favoritedAt: Long = 0L) = Movie(
        movieId = id,
        title = title,
        posterUrl = "",
        backdropUrl = "",
        overview = "",
        releaseDate = "2024-01-01",
        voteAverage = 7.0,
        voteCount = 10,
        runtime = 100,
        status = "Released",
        tagline = "",
        genres = listOf("Drama"),
        company = "",
        language = "en",
        favoritedAt = favoritedAt
    )
}

private class FakeFavoriteUseCase : MovieUseCase {
    val favorites = MutableStateFlow<List<Movie>>(emptyList())
    val added = mutableListOf<Movie>()
    val removed = mutableListOf<Int>()

    override fun getMovies(category: MovieCategory): Flow<Resource<List<Movie>>> = emptyFlow()

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> = emptyFlow()

    override fun getMovieDetail(movieId: Int): Flow<Resource<Movie>> = emptyFlow()

    override fun getFavoriteMovies(): Flow<List<Movie>> = favorites

    override fun isFavorite(movieId: Int): Flow<Boolean> = flowOf(false)

    override suspend fun addFavorite(movie: Movie) {
        added += movie
        favorites.update { it + movie }
    }

    override suspend fun removeFavorite(movieId: Int) {
        removed += movieId
        favorites.update { list -> list.filterNot { it.movieId == movieId } }
    }
}
