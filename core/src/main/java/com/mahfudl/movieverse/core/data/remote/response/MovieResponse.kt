package com.mahfudl.movieverse.core.data.remote.response

import com.google.gson.annotations.SerializedName

/** Model layer DATA (response TMDB API). */
data class MovieListResponse(@SerializedName("results") val results: List<MovieDto>?)

/**
 * Dipakai untuk endpoint list maupun detail. Field yang hanya ada di detail
 * (runtime, genres, dst.) bernilai null pada endpoint list.
 */
data class MovieDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    @SerializedName("genres") val genres: List<NamedDto>?,
    @SerializedName("production_companies") val productionCompanies: List<NamedDto>?
)

data class NamedDto(@SerializedName("name") val name: String?)
