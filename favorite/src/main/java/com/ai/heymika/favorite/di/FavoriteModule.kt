package com.ai.heymika.favorite.di

import com.ai.heymika.favorite.presentation.FavoriteViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin definitions owned by the `:favorite` dynamic feature.
 *
 * Loaded by [com.ai.heymika.favorite.FavoriteEntry] through `loadKoinModules`. It is deliberately
 * never unloaded: the definitions are cheap, the module stays installed for the rest of the
 * process, and unloading on dispose would drop the definitions while the destination is still
 * on the navigation back stack.
 */
val favoriteModule =
    module {
        viewModel { FavoriteViewModel(materialUseCase = get()) }
    }
