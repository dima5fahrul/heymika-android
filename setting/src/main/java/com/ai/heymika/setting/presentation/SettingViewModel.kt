package com.ai.heymika.setting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.heymika.core.domain.usecase.SettingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingUseCase: SettingUseCase,
) : ViewModel() {
    val state: StateFlow<SettingUiState> =
        settingUseCase.dailyReminder
            .map { dailyReminder ->
                SettingUiState(
                    isLoading = false,
                    dailyReminder = dailyReminder,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SettingUiState(),
            )

    fun setDailyReminder(enabled: Boolean) {
        viewModelScope.launch { settingUseCase.setDailyReminder(enabled) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
