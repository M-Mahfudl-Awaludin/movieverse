package com.mahfudl.movieverse.core

import com.mahfudl.movieverse.core.data.local.MovieEntity
import com.mahfudl.movieverse.core.data.remote.response.MovieDto
import com.mahfudl.movieverse.core.data.remote.response.NamedDto
import com.mahfudl.movieverse.core.domain.model.Movie

internal fun testMovie(id: Int = 1, title: String = "Movie $id", favoritedAt: Long = 0L) = Movie(
    movieId = id,
    title = title,
    posterUrl = "https://image.tmdb.org/t/p/w500/poster$id.jpg",
    backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop$id.jpg",
    overview = "Overview $id",
    releaseDate = "2024-05-01",
    voteAverage = 7.2,
    voteCount = 1_234,
    runtime = 121,
    status = "Released",
    tagline = "Tagline $id",
    genres = listOf("Action", "Drama"),
    company = "Studio",
    language = "en",
    favoritedAt = favoritedAt
)

internal fun testEntity(id: Int = 1, addedAt: Long = 1_000L) = MovieEntity(
    movieId = id,
    title = "Movie $id",
    posterUrl = "",
    backdropUrl = "",
    overview = "",
    releaseDate = "",
    voteAverage = null,
    voteCount = null,
    runtime = null,
    status = "",
    tagline = "",
    genres = "Action|Drama",
    company = "",
    language = "en",
    addedAt = addedAt
)

internal fun testDto(
    id: Int? = 1,
    title: String? = "Movie $id",
    posterPath: String? = "/poster.jpg",
    backdropPath: String? = "/backdrop.jpg",
    genreIds: List<Int>? = null,
    genres: List<NamedDto>? = null,
    companies: List<NamedDto>? = null
) = MovieDto(
    id = id,
    title = title,
    overview = "Overview",
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = "2010-07-16",
    voteAverage = 8.4,
    voteCount = 35_000,
    runtime = 148,
    status = "Released",
    tagline = "Tagline",
    originalLanguage = "en",
    genreIds = genreIds,
    genres = genres,
    productionCompanies = companies
)
