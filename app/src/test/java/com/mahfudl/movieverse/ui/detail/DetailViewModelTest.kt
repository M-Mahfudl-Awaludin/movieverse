package com.mahfudl.movieverse.ui.detail

import com.mahfudl.movieverse.FakeMovieUseCase
import com.mahfudl.movieverse.MainDispatcherRule
import com.mahfudl.movieverse.collectStates
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.testMovie
import com.mahfudl.movieverse.ui.model.MovieUi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val useCase = FakeMovieUseCase()

    @Test
    fun exposesMappedMovieDetail() = runTest {
        useCase.detail = Resource.Success(testMovie(4, "Interstellar"))
        val viewModel = DetailViewModel(4, useCase)

        val states = collectStates(viewModel.state)

        val last = states.last() as Resource.Success<MovieUi>
        assertEquals("Interstellar", last.data.title)
    }

    @Test
    fun stateStaysLoadingUntilDataArrives() = runTest {
        val viewModel = DetailViewModel(4, useCase)

        val states = collectStates(viewModel.state)

        assertEquals(Resource.Loading, states.last())
    }

    @Test
    fun toggleAddsMovieToFavoritesThenRemovesIt() = runTest {
        useCase.detail = Resource.Success(testMovie(4))
        val viewModel = DetailViewModel(4, useCase)
        collectStates(viewModel.state)
        collectStates(viewModel.isFavorite)
        assertFalse(viewModel.isFavorite.value)

        viewModel.toggleFavorite()

        assertEquals(listOf(4), useCase.added.map { it.movieId })
        assertTrue(viewModel.isFavorite.value)

        viewModel.toggleFavorite()

        assertEquals(listOf(4), useCase.removed)
        assertFalse(viewModel.isFavorite.value)
    }

    @Test
    fun toggleBeforeDataIsLoadedDoesNothing() = runTest {
        val viewModel = DetailViewModel(4, useCase)
        collectStates(viewModel.state)
        collectStates(viewModel.isFavorite)

        viewModel.toggleFavorite()

        assertTrue(useCase.added.isEmpty())
        assertTrue(useCase.removed.isEmpty())
    }

    @Test
    fun retryReloadsTheDetail() = runTest {
        useCase.detail = Resource.Error("offline")
        val viewModel = DetailViewModel(4, useCase)
        val states = collectStates(viewModel.state)
        assertEquals(1, useCase.detailRequests)

        useCase.detail = Resource.Success(testMovie(4))
        viewModel.retry()

        assertEquals(2, useCase.detailRequests)
        assertTrue(states.last() is Resource.Success<*>)
    }
}
