package com.mahfudl.movieverse.core.data.remote

import com.mahfudl.movieverse.core.data.remote.response.MovieDto
import com.mahfudl.movieverse.core.domain.model.MovieCategory

internal class RemoteDataSource(private val api: ApiService) {

    suspend fun getMovies(category: MovieCategory): List<MovieDto> {
        val response = when (category) {
            MovieCategory.POPULAR -> api.getPopular()
            MovieCategory.TOP_RATED -> api.getTopRated()
            MovieCategory.NOW_PLAYING -> api.getNowPlaying()
            MovieCategory.UPCOMING -> api.getUpcoming()
        }
        return response.results.orEmpty()
    }

    suspend fun searchMovies(query: String): List<MovieDto> = api.searchMovies(query).results.orEmpty()

    suspend fun getMovieDetail(movieId: Int): MovieDto = api.getMovieDetail(movieId)
}
