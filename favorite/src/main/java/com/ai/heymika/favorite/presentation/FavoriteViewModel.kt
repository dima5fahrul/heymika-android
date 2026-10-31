package com.ai.heymika.favorite.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.usecase.MaterialUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModel(
    private val materialUseCase: MaterialUseCase,
) : ViewModel() {
    /** Bumped by [retry] so a failed read can be re-collected from scratch. */
    private val reloads = MutableStateFlow(0)

    val state: StateFlow<FavoriteUiState> =
        reloads
            .flatMapLatest {
                materialUseCase
                    .getFavoriteMaterials()
                    .map { materials -> FavoriteUiState(isLoading = false, materials = materials) }
                    .onStart { emit(FavoriteUiState()) }
                    .catch { throwable ->
                        emit(FavoriteUiState(isLoading = false, error = AppError.Unknown(throwable)))
                    }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = FavoriteUiState(),
            )

    fun retry() {
        reloads.value += 1
    }

    /** The card's favourite button can only ever mean "remove" on this screen. */
    fun removeFromFavorite(material: Material) {
        viewModelScope.launch { materialUseCase.setFavorite(material, false) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
