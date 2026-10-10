package com.mahfudl.movieverse.core.data.local

import kotlinx.coroutines.flow.Flow

internal class LocalDataSource(private val dao: MovieDao) {
    fun getAll(): Flow<List<MovieEntity>> = dao.getAll()
    suspend fun getById(movieId: Int): MovieEntity? = dao.getById(movieId)
    fun isFavorite(movieId: Int): Flow<Boolean> = dao.isFavorite(movieId)
    suspend fun insert(entity: MovieEntity) = dao.insert(entity)
    suspend fun delete(movieId: Int) = dao.delete(movieId)
}
