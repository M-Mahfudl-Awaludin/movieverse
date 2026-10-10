package com.mahfudl.movieverse.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Model layer DATA (tabel database). Setiap baris = 1 film favorit. */
@Entity(tableName = "favorite_movie")
data class MovieEntity(
    @PrimaryKey val movieId: Int,
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
    val genres: String,
    val company: String,
    val language: String,
    val addedAt: Long = System.currentTimeMillis()
)
