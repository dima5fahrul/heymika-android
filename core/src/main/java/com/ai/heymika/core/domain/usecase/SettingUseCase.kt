package com.ai.heymika.core.domain.usecase

import kotlinx.coroutines.flow.Flow

interface SettingUseCase {
    val darkMode: Flow<Boolean>

    suspend fun setDarkMode(enabled: Boolean)

    val dailyReminder: Flow<Boolean>

    suspend fun setDailyReminder(enabled: Boolean)
}
