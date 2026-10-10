package com.mahfudl.movieverse.ui.home

import com.mahfudl.movieverse.FakeMovieUseCase
import com.mahfudl.movieverse.MainDispatcherRule
import com.mahfudl.movieverse.collectStates
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.testMovie
import com.mahfudl.movieverse.ui.model.MovieUi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val useCase = FakeMovieUseCase()

    @Test
    fun loadsPopularMoviesByDefault() = runTest {
        useCase.moviesByCategory[MovieCategory.POPULAR] = Resource.Success(listOf(testMovie(1, "Inception")))
        val viewModel = HomeViewModel(useCase)

        val states = collectStates(viewModel.state)

        val last = states.last() as Resource.Success<List<MovieUi>>
        assertEquals(listOf("Inception"), last.data.map { it.title })
        assertEquals(MovieCategory.POPULAR, viewModel.selectedCategory.value)
    }

    @Test
    fun changingCategoryLoadsThatCategory() = runTest {
        useCase.moviesByCategory[MovieCategory.TOP_RATED] = Resource.Success(listOf(testMovie(2, "Parasite")))
        val viewModel = HomeViewModel(useCase)
        val states = collectStates(viewModel.state)

        viewModel.setCategory(MovieCategory.TOP_RATED)

        assertEquals(MovieCategory.TOP_RATED, viewModel.selectedCategory.value)
        assertEquals(listOf(MovieCategory.POPULAR, MovieCategory.TOP_RATED), useCase.requestedCategories)
        val last = states.last() as Resource.Success<List<MovieUi>>
        assertEquals(listOf("Parasite"), last.data.map { it.title })
    }

    @Test
    fun errorIsExposedToTheUi() = runTest {
        useCase.moviesByCategory[MovieCategory.POPULAR] = Resource.Error("boom")
        val viewModel = HomeViewModel(useCase)

        val states = collectStates(viewModel.state)

        assertEquals(Resource.Error("boom"), states.last())
    }

    @Test
    fun retryRequestsTheSameCategoryAgain() = runTest {
        useCase.moviesByCategory[MovieCategory.POPULAR] = Resource.Error("boom")
        val viewModel = HomeViewModel(useCase)
        collectStates(viewModel.state)

        useCase.moviesByCategory[MovieCategory.POPULAR] = Resource.Success(listOf(testMovie(1)))
        viewModel.retry()

        assertEquals(listOf(MovieCategory.POPULAR, MovieCategory.POPULAR), useCase.requestedCategories)
        assertEquals(1, ((viewModel.state.value) as Resource.Success<List<MovieUi>>).data.size)
    }
}
