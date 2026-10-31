package com.ai.heymika.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingRepository {
    val darkMode: Flow<Boolean>

    suspend fun setDarkMode(enabled: Boolean)

    val dailyReminder: Flow<Boolean>

    suspend fun setDailyReminder(enabled: Boolean)
}
