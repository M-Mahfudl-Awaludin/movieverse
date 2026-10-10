package com.mahfudl.movieverse.core.domain.model

/** Model layer DOMAIN (murni Kotlin, tidak bergantung pada Android / library data). */
data class Movie(
    val movieId: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val overview: String,
    val releaseDate: String,
    val voteAverage: Double?,
    val voteCount: Int?,
    val runtime: Int?,
    val status: String,
    val tagline: String,
    val genres: List<String>,
    val company: String,
    val language: String,
    /** Waktu ditambahkan ke favorit (0 = belum difavoritkan). Dipakai agar urutan tetap saat undo. */
    val favoritedAt: Long = 0L
)
