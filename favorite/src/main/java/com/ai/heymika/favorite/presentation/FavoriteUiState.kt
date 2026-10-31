package com.ai.heymika.favorite.presentation

import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.Material

/** Everything [FavoriteScreen] needs in order to render. */
data class FavoriteUiState(
    val isLoading: Boolean = true,
    val materials: List<Material> = emptyList(),
    val error: AppError? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && error == null && materials.isEmpty()
}
