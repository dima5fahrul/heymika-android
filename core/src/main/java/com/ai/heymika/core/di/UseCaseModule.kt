package com.ai.heymika.core.di

import com.ai.heymika.core.domain.usecase.AiInteractor
import com.ai.heymika.core.domain.usecase.AiUseCase
import com.ai.heymika.core.domain.usecase.MaterialInteractor
import com.ai.heymika.core.domain.usecase.MaterialUseCase
import com.ai.heymika.core.domain.usecase.SettingInteractor
import com.ai.heymika.core.domain.usecase.SettingUseCase
import org.koin.dsl.module

val useCaseModule =
    module {
        factory<MaterialUseCase> { MaterialInteractor(get()) }
        factory<AiUseCase> { AiInteractor(get()) }
        factory<SettingUseCase> { SettingInteractor(get()) }
    }
