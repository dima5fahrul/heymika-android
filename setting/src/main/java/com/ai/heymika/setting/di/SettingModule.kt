package com.ai.heymika.setting.di

import com.ai.heymika.setting.presentation.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin definitions owned by the `:setting` dynamic feature.
 *
 * Loaded by [com.ai.heymika.setting.SettingEntry] through `loadKoinModules`. It is deliberately
 * never unloaded: the definitions are cheap, the module stays installed for the rest of the
 * process, and unloading on dispose would drop the definitions while the destination is still
 * on the navigation back stack.
 */
val settingModule =
    module {
        viewModel { SettingViewModel(settingUseCase = get()) }
    }
