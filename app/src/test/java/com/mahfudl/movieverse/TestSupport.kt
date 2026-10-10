package com.mahfudl.movieverse

import com.mahfudl.movieverse.core.domain.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Mulai mengumpulkan [flow] di latar belakang (otomatis berhenti saat test selesai) dan
 * kembalikan daftar nilai yang diterima sejauh ini.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.collectStates(flow: Flow<T>): List<T> {
    val states = mutableListOf<T>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(states) }
    return states
}

@Suppress("LongParameterList")
fun testMovie(
    id: Int = 1,
    title: String = "Movie $id",
    releaseDate: String = "2024-05-01",
    voteAverage: Double? = 7.2,
    voteCount: Int? = 1_234,
    runtime: Int? = 121,
    genres: List<String> = listOf("Action", "Drama"),
    company: String = "Studio",
    language: String = "en",
    status: String = "Released",
    favoritedAt: Long = 0L
) = Movie(
    movieId = id,
    title = title,
    posterUrl = "https://image.tmdb.org/t/p/w500/poster$id.jpg",
    backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop$id.jpg",
    overview = "Overview $id",
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    runtime = runtime,
    status = status,
    tagline = "Tagline $id",
    genres = genres,
    company = company,
    language = language,
    favoritedAt = favoritedAt
)
