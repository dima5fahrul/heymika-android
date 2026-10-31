package com.ai.heymika.aistudio.di

import com.ai.heymika.aistudio.presentation.AiStudioViewModel
import com.ai.heymika.core.domain.model.GenerationKind
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin definitions owned by the `:aistudio` dynamic feature.
 *
 * Loaded by [com.ai.heymika.aistudio.AiStudioEntry] through `loadKoinModules`. It is deliberately
 * never unloaded: the definitions are cheap, the module stays installed for the rest of the
 * process, and unloading on dispose would drop the definitions while the destination is still
 * on the navigation back stack - including while a `Resource.Loading` generation is in flight.
 *
 * `materialId` and `kind` come from the navigation arguments, so they are passed as runtime
 * parameters rather than resolved from the graph.
 */
val aiStudioModule =
    module {
        viewModel { params ->
            AiStudioViewModel(
                materialId = params.get<String>(),
                kind = params.get<GenerationKind>(),
                aiUseCase = get(),
            )
        }
    }
