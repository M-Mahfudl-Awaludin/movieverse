package com.mahfudl.movieverse.ui.model

import com.mahfudl.movieverse.core.domain.model.Movie
import java.util.Locale

fun Movie.toUi(): MovieUi {
    val year = releaseDate.take(4).takeIf { it.length == 4 }
    val runtimeLabel = runtime?.takeIf { it > 0 }?.let { "${it / 60}j ${it % 60}m" }
    return MovieUi(
        movieId = movieId,
        title = title,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        metaText = listOfNotNull(year, genres.firstOrNull()).joinToString(" • ").ifBlank { "-" },
        subtitle = listOfNotNull(year, runtimeLabel, company.ifBlank { null })
            .joinToString(" • ").ifBlank { "-" },
        scoreText = voteAverage?.takeIf { it > 0 }?.let { String.format(Locale.US, "%.1f", it) },
        votesText = voteCount?.let { formatCount(it) } ?: "-",
        runtimeText = runtimeLabel ?: "-",
        status = status.ifBlank { "-" },
        releaseText = releaseDate.ifBlank { "-" },
        languageText = language.uppercase(Locale.ROOT).ifBlank { "-" },
        overview = overview,
        genres = genres
    )
}

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
    else -> count.toString()
}
