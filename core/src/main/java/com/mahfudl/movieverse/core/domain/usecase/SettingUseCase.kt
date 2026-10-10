package com.mahfudl.movieverse.core.domain.usecase

import com.mahfudl.movieverse.core.domain.repository.ISettingRepository
import kotlinx.coroutines.flow.Flow

interface SettingUseCase {
    fun getDarkMode(): Flow<Boolean?>
    suspend fun setDarkMode(enabled: Boolean?)
}

class SettingInteractor(private val repository: ISettingRepository) : SettingUseCase {
    override fun getDarkMode() = repository.getDarkMode()
    override suspend fun setDarkMode(enabled: Boolean?) = repository.setDarkMode(enabled)
}
