package com.ai.heymika.core.domain.usecase

import com.ai.heymika.core.domain.repository.ISettingRepository
import kotlinx.coroutines.flow.Flow

class SettingInteractor(
    private val settingRepository: ISettingRepository,
) : SettingUseCase {
    override val darkMode: Flow<Boolean>
        get() = settingRepository.darkMode

    override suspend fun setDarkMode(enabled: Boolean) = settingRepository.setDarkMode(enabled)

    override val dailyReminder: Flow<Boolean>
        get() = settingRepository.dailyReminder

    override suspend fun setDailyReminder(enabled: Boolean) = settingRepository.setDailyReminder(enabled)
}
