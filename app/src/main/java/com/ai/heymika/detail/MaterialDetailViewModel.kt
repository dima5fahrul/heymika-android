package com.ai.heymika.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.usecase.MaterialUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MaterialDetailUiState(
    val isLoading: Boolean = true,
    val material: Material? = null,
)

/**
 * `MaterialUseCase.getMaterialById` is a cold [kotlinx.coroutines.flow.Flow] of the stored
 * material, so the favourite toggle below needs no local state: writing through the use case
 * re-emits the updated [Material].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MaterialDetailViewModel(
    private val materialUseCase: MaterialUseCase,
) : ViewModel() {
    private val materialId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MaterialDetailUiState> =
        materialId
            .flatMapLatest { id ->
                if (id.isNullOrBlank()) {
                    flowOf(MaterialDetailUiState(isLoading = false, material = null))
                } else {
                    materialUseCase.getMaterialById(id).map { material ->
                        MaterialDetailUiState(isLoading = false, material = material)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = MaterialDetailUiState(),
            )

    /** Called from the screen with the `materialId` nav argument; idempotent. */
    fun bind(id: String) {
        if (materialId.value != id) {
            materialId.value = id
        }
    }

    fun toggleFavorite() {
        val material = uiState.value.material ?: return
        viewModelScope.launch {
            materialUseCase.setFavorite(material, !material.isFavorite)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
