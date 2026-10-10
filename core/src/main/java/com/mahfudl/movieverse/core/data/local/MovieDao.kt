package com.mahfudl.movieverse.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM favorite_movie ORDER BY addedAt DESC")
    fun getAll(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM favorite_movie WHERE movieId = :movieId LIMIT 1")
    suspend fun getById(movieId: Int): MovieEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movie WHERE movieId = :movieId)")
    fun isFavorite(movieId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MovieEntity)

    @Query("DELETE FROM favorite_movie WHERE movieId = :movieId")
    suspend fun delete(movieId: Int)
}
