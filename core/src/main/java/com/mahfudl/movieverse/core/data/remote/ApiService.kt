package com.mahfudl.movieverse.core.data.remote

import com.mahfudl.movieverse.core.data.remote.response.MovieDto
import com.mahfudl.movieverse.core.data.remote.response.MovieListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TheMovieDB (TMDB) API v3. Parameter api_key ditambahkan otomatis oleh interceptor. */
interface ApiService {

    @GET("movie/popular")
    suspend fun getPopular(@Query("page") page: Int = 1): MovieListResponse

    @GET("movie/top_rated")
    suspend fun getTopRated(@Query("page") page: Int = 1): MovieListResponse

    @GET("movie/now_playing")
    suspend fun getNowPlaying(@Query("page") page: Int = 1): MovieListResponse

    @GET("movie/upcoming")
    suspend fun getUpcoming(@Query("page") page: Int = 1): MovieListResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/{id}")
    suspend fun getMovieDetail(@Path("id") id: Int): MovieDto
}
