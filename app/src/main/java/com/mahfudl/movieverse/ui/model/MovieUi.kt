package com.mahfudl.movieverse.ui.model

/** Model layer PRESENTATION (sudah siap ditampilkan di UI). */
data class MovieUi(
    val movieId: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val metaText: String,
    val subtitle: String,
    val scoreText: String?,
    val votesText: String,
    val runtimeText: String,
    val status: String,
    val releaseText: String,
    val languageText: String,
    val overview: String,
    val genres: List<String>
)
