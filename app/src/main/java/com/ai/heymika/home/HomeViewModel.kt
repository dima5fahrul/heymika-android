package com.ai.heymika.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.usecase.MaterialUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Browse state of the material catalogue. [materials] keeps the [Resource] wrapper so the
 * screen can pick the matching `:core` state composable, while already being filtered.
 */
data class HomeUiState(
    val query: String = "",
    val selectedSubject: String? = null,
    val subjects: List<String> = emptyList(),
    val materials: Resource<List<Material>> = Resource.Loading(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val materialUseCase: MaterialUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedSubject = MutableStateFlow<String?>(null)

    /** Bumped by [retry] so a failed load can be re-collected from scratch. */
    private val reloads = MutableStateFlow(0)

    private val materials = reloads.flatMapLatest { materialUseCase.getAllMaterials() }

    val uiState: StateFlow<HomeUiState> =
        combine(
            materials,
            query,
            selectedSubject,
        ) { resource, currentQuery, subject ->
            HomeUiState(
                query = currentQuery,
                selectedSubject = subject,
                subjects = dataOf(resource).map { it.subject }.distinct().sorted(),
                materials = filtered(resource, currentQuery, subject),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Selecting the active subject again clears the filter. */
    fun onSubjectSelected(subject: String?) {
        selectedSubject.value = if (subject != null && selectedSubject.value == subject) null else subject
    }

    fun retry() {
        reloads.value += 1
    }

    fun toggleFavorite(material: Material) {
        viewModelScope.launch {
            materialUseCase.setFavorite(material, !material.isFavorite)
        }
    }

    /** `Loading` and `Error` may carry the last known good list, so never drop it. */
    private fun dataOf(resource: Resource<List<Material>>): List<Material> = resource.data.orEmpty()

    private fun filtered(
        resource: Resource<List<Material>>,
        searchText: String,
        subject: String?,
    ): Resource<List<Material>> {
        val matching =
            dataOf(resource).filter { material ->
                val matchesQuery =
                    searchText.isBlank() ||
                        material.title.contains(searchText, ignoreCase = true) ||
                        material.subject.contains(searchText, ignoreCase = true) ||
                        material.description.contains(searchText, ignoreCase = true)
                val matchesSubject = subject == null || material.subject == subject
                matchesQuery && matchesSubject
            }
        return when (resource) {
            is Resource.Loading -> Resource.Loading(matching)
            is Resource.Success -> Resource.Success(matching)
            is Resource.Error -> Resource.Error(resource.message, matching, resource.error)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
