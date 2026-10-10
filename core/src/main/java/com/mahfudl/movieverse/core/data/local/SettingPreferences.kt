package com.mahfudl.movieverse.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

internal class SettingPreferences(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    fun darkMode(): Flow<Boolean?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[darkModeKey] }
        .distinctUntilChanged()

    suspend fun setDarkMode(enabled: Boolean?) {
        context.dataStore.edit { if (enabled == null) it.remove(darkModeKey) else it[darkModeKey] = enabled }
    }
}
