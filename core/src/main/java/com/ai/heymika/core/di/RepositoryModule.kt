package com.ai.heymika.core.di

import com.ai.heymika.core.data.repository.AiRepository
import com.ai.heymika.core.data.repository.MaterialRepository
import com.ai.heymika.core.data.repository.SettingRepository
import com.ai.heymika.core.domain.repository.IAiRepository
import com.ai.heymika.core.domain.repository.IMaterialRepository
import com.ai.heymika.core.domain.repository.ISettingRepository
import org.koin.dsl.module

val repositoryModule =
    module {
        single<IMaterialRepository> { MaterialRepository(get(), get()) }
        single<IAiRepository> { AiRepository(get(), get()) }
        single<ISettingRepository> { SettingRepository(get()) }
    }
