package com.ai.heymika.core.data.repository

import com.ai.heymika.core.data.source.local.datastore.SettingPreferences
import com.ai.heymika.core.domain.repository.ISettingRepository
import kotlinx.coroutines.flow.Flow

class SettingRepository(
    private val settingPreferences: SettingPreferences,
) : ISettingRepository {
    override val darkMode: Flow<Boolean>
        get() = settingPreferences.getDarkMode()

    override suspend fun setDarkMode(enabled: Boolean) = settingPreferences.setDarkMode(enabled)

    override val dailyReminder: Flow<Boolean>
        get() = settingPreferences.getDailyReminder()

    override suspend fun setDailyReminder(enabled: Boolean) = settingPreferences.setDailyReminder(enabled)
}
