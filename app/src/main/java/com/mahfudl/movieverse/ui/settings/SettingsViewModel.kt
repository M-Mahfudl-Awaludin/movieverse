package com.mahfudl.movieverse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahfudl.movieverse.core.domain.usecase.SettingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val useCase: SettingUseCase) : ViewModel() {

    /** null = mengikuti sistem */
    val darkMode: StateFlow<Boolean?> = useCase.getDarkMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch { useCase.setDarkMode(enabled) }
    }
}
