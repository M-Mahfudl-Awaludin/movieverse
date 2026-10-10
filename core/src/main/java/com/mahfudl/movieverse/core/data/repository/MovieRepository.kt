package com.mahfudl.movieverse.core.data.repository

import com.mahfudl.movieverse.core.data.local.LocalDataSource
import com.mahfudl.movieverse.core.data.remote.RemoteDataSource
import com.mahfudl.movieverse.core.domain.model.Movie
import com.mahfudl.movieverse.core.domain.model.MovieCategory
import com.mahfudl.movieverse.core.domain.model.Resource
import com.mahfudl.movieverse.core.domain.repository.IMovieRepository
import com.mahfudl.movieverse.core.utils.toDomain
import com.mahfudl.movieverse.core.utils.toEntity
import com.mahfudl.movieverse.core.utils.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class MovieRepository(private val remote: RemoteDataSource, private val local: LocalDataSource) :
    IMovieRepository {

    override fun getMovies(category: MovieCategory): Flow<Resource<List<Movie>>> = flow<Resource<List<Movie>>> {
        emit(Resource.Loading)
        emit(Resource.Success(remote.getMovies(category).toDomain()))
    }.catch { emit(Resource.Error(it.toUserMessage())) }
        .flowOn(Dispatchers.IO)

    override fun searchMovies(query: String): Flow<Resource<List<Movie>>> = flow<Resource<List<Movie>>> {
        emit(Resource.Loading)
        emit(Resource.Success(remote.searchMovies(query).toDomain()))
    }.catch { emit(Resource.Error(it.toUserMessage())) }
        .flowOn(Dispatchers.IO)

    /** Ambil dari network; bila gagal, fallback ke data favorit lokal (mode offline). */
    override fun getMovieDetail(movieId: Int): Flow<Resource<Movie>> = flow<Resource<Movie>> {
        emit(Resource.Loading)
        var fetched: Movie? = null
        var failure: Throwable? = null
        try {
            fetched = remote.getMovieDetail(movieId).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failure = e
        }
        if (fetched != null) {
            emit(Resource.Success(fetched))
        } else {
            val cached = safeLocal(null) { local.getById(movieId)?.toDomain() }
            if (cached != null) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Error((failure ?: IllegalStateException()).toUserMessage()))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getFavoriteMovies(): Flow<List<Movie>> = local.getAll()
        .map { list -> list.map { it.toDomain() } }
        .catch { emit(emptyList()) }
        .flowOn(Dispatchers.Default)

    override fun isFavorite(movieId: Int): Flow<Boolean> = local.isFavorite(movieId).catch { emit(false) }

    override suspend fun addFavorite(movie: Movie) {
        safeLocal(Unit) { local.insert(movie.toEntity()) }
    }

    override suspend fun removeFavorite(movieId: Int) {
        safeLocal(Unit) { local.delete(movieId) }
    }

    /**
     * Operasi database yang gagal (mis. berkas rusak) tidak boleh membuat aplikasi force close;
     * hasilnya diganti [fallback] sementara pembatalan coroutine tetap diteruskan.
     */
    private inline fun <T> safeLocal(fallback: T, block: () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        fallback
    }
}
