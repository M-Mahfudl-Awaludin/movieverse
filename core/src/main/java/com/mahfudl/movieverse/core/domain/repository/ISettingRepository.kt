package com.mahfudl.movieverse.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingRepository {
    /** null = ikuti pengaturan sistem */
    fun getDarkMode(): Flow<Boolean?>

    /** null = kembali mengikuti sistem */
    suspend fun setDarkMode(enabled: Boolean?)
}
