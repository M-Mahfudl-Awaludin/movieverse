package com.mahfudl.movieverse.core.utils

import com.mahfudl.movieverse.core.data.local.MovieEntity
import com.mahfudl.movieverse.core.data.remote.response.MovieDto
import com.mahfudl.movieverse.core.domain.model.Movie
import java.io.IOException
import retrofit2.HttpException

private const val GENRE_SEPARATOR = "|"
private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

/** Daftar genre TMDB (id stabil) untuk endpoint list yang hanya mengirim genre_ids. */
private val GENRE_NAMES = mapOf(
    28 to "Action", 12 to "Adventure", 16 to "Animation", 35 to "Comedy",
    80 to "Crime", 99 to "Documentary", 18 to "Drama", 10751 to "Family",
    14 to "Fantasy", 36 to "History", 27 to "Horror", 10402 to "Music",
    9648 to "Mystery", 10749 to "Romance", 878 to "Science Fiction",
    10770 to "TV Movie", 53 to "Thriller", 10752 to "War", 37 to "Western"
)

fun MovieDto.toDomain(): Movie? {
    val movieId = id ?: return null
    val genreNames = genres.orEmpty().mapNotNull { it.name }
        .ifEmpty { genreIds.orEmpty().mapNotNull { GENRE_NAMES[it] } }
    return Movie(
        movieId = movieId,
        title = title?.takeIf { it.isNotBlank() } ?: "Tanpa judul",
        posterUrl = posterPath?.let { POSTER_BASE_URL + it }.orEmpty(),
        backdropUrl = backdropPath?.let { BACKDROP_BASE_URL + it }.orEmpty(),
        overview = overview.orEmpty(),
        releaseDate = releaseDate.orEmpty(),
        voteAverage = voteAverage,
        voteCount = voteCount,
        runtime = runtime,
        status = status.orEmpty(),
        tagline = tagline.orEmpty(),
        genres = genreNames,
        company = productionCompanies.orEmpty().firstNotNullOfOrNull { it.name }.orEmpty(),
        language = originalLanguage.orEmpty()
    )
}

fun List<MovieDto>.toDomain(): List<Movie> = mapNotNull { it.toDomain() }.distinctBy { it.movieId }

fun MovieEntity.toDomain() = Movie(
    movieId = movieId,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    runtime = runtime,
    status = status,
    tagline = tagline,
    genres = if (genres.isBlank()) emptyList() else genres.split(GENRE_SEPARATOR),
    company = company,
    language = language,
    favoritedAt = addedAt
)

fun Movie.toEntity() = MovieEntity(
    movieId = movieId,
    title = title,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    runtime = runtime,
    status = status,
    tagline = tagline,
    genres = genres.joinToString(GENRE_SEPARATOR),
    company = company,
    language = language,
    addedAt = if (favoritedAt > 0L) favoritedAt else System.currentTimeMillis()
)

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> when (code()) {
        401 -> "API key TMDB tidak valid. Periksa TMDB_API_KEY di gradle.properties."
        429 -> "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi."
        else -> "Server sedang bermasalah (kode ${code()})."
    }
    is IOException -> "Tidak ada koneksi internet. Periksa jaringan Anda."
    else -> message ?: "Terjadi kesalahan tak terduga."
}
