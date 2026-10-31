package com.ai.heymika.setting.presentation

/** Everything [SettingScreen] needs in order to render. */
data class SettingUiState(
    val isLoading: Boolean = true,
    val dailyReminder: Boolean = false,
)
