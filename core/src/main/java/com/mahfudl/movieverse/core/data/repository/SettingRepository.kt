package com.mahfudl.movieverse.core.data.repository

import com.mahfudl.movieverse.core.data.local.SettingPreferences
import com.mahfudl.movieverse.core.domain.repository.ISettingRepository
import kotlinx.coroutines.flow.Flow

internal class SettingRepository(private val preferences: SettingPreferences) : ISettingRepository {
    override fun getDarkMode(): Flow<Boolean?> = preferences.darkMode()
    override suspend fun setDarkMode(enabled: Boolean?) = preferences.setDarkMode(enabled)
}
