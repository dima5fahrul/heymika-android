package com.ai.heymika.di

import com.ai.heymika.detail.MaterialDetailViewModel
import com.ai.heymika.home.HomeViewModel
import com.ai.heymika.navigation.DynamicFeatureInstaller
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Everything owned by `:app`. The data/network/database/use-case graph lives in `:core`.
 */
val appModule =
    module {
        // App-scoped: it registers a listener on SplitInstallManager and caches the reflectively
        // loaded FeatureEntry instances, so there must be exactly one.
        single { DynamicFeatureInstaller(androidContext()) }

        viewModel { HomeViewModel(get()) }
        viewModel { MaterialDetailViewModel(get()) }
    }
