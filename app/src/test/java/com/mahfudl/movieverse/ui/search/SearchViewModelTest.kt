package com.mahfudl.movieverse.ui.search

import com.mahfudl.movieverse.FakeMovieUseCase
import com.mahfudl.movieverse.MainDispatcherRule
import com.mahfudl.movieverse.collectStates
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.testMovie
import com.mahfudl.movieverse.ui.model.MovieUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val useCase = FakeMovieUseCase()

    @Test
    fun startsIdle() = runTest {
        val viewModel = SearchViewModel(useCase)

        val states = collectStates(viewModel.state)
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()

        assertNull(states.last())
        assertTrue(useCase.searchedQueries.isEmpty())
    }

    @Test
    fun queryShorterThanTwoCharactersDoesNotSearch() = runTest {
        val viewModel = SearchViewModel(useCase)
        val states = collectStates(viewModel.state)

        viewModel.setQuery("a")
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()

        assertNull(states.last())
        assertTrue(useCase.searchedQueries.isEmpty())
    }

    @Test
    fun searchWaitsForDebounceThenEmitsResults() = runTest {
        useCase.searchResult = Resource.Success(listOf(testMovie(1, "Batman")))
        val viewModel = SearchViewModel(useCase)
        val states = collectStates(viewModel.state)

        viewModel.setQuery("bat")
        advanceTimeBy(DEBOUNCE_MS - 1)
        runCurrent()
        assertTrue("Belum boleh mencari sebelum debounce habis", useCase.searchedQueries.isEmpty())

        advanceTimeBy(2)
        runCurrent()

        assertEquals(listOf("bat"), useCase.searchedQueries)
        val last = states.last() as Resource.Success<List<MovieUi>>
        assertEquals(listOf("Batman"), last.data.map { it.title })
    }

    @Test
    fun rapidTypingOnlySearchesForTheLastQuery() = runTest {
        val viewModel = SearchViewModel(useCase)
        collectStates(viewModel.state)

        viewModel.setQuery("ba")
        advanceTimeBy(100)
        viewModel.setQuery("bat")
        advanceTimeBy(100)
        viewModel.setQuery("batman")
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()

        assertEquals(listOf("batman"), useCase.searchedQueries)
    }

    @Test
    fun queryIsTrimmed() = runTest {
        val viewModel = SearchViewModel(useCase)
        collectStates(viewModel.state)

        viewModel.setQuery("  matrix  ")
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()

        assertEquals(listOf("matrix"), useCase.searchedQueries)
    }

    @Test
    fun clearingTheQueryReturnsToIdle() = runTest {
        val viewModel = SearchViewModel(useCase)
        val states = collectStates(viewModel.state)
        viewModel.setQuery("matrix")
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()

        viewModel.setQuery("")
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()

        assertNull(states.last())
    }

    @Test
    fun errorCanBeRetried() = runTest {
        useCase.searchResult = Resource.Error("offline")
        val viewModel = SearchViewModel(useCase)
        val states = collectStates(viewModel.state)
        viewModel.setQuery("matrix")
        advanceTimeBy(DEBOUNCE_MS * 2)
        runCurrent()
        assertEquals(Resource.Error("offline"), states.last())

        useCase.searchResult = Resource.Success(listOf(testMovie(1)))
        viewModel.retry()

        assertEquals(listOf("matrix", "matrix"), useCase.searchedQueries)
        assertTrue(states.last() is Resource.Success<*>)
    }

    private companion object {
        const val DEBOUNCE_MS = 500L
    }
}
