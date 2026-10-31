package com.ai.heymika.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.heymika.R
import com.ai.heymika.core.data.Resource
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.MaterialCard
import com.ai.heymika.core.ui.component.display.FilterChipItem
import com.ai.heymika.core.ui.component.display.FilterChipRow
import com.ai.heymika.core.ui.component.input.SearchField
import com.ai.heymika.core.ui.component.state.MikaEmptyState
import com.ai.heymika.core.ui.component.state.MikaErrorState
import com.ai.heymika.core.ui.component.state.MikaLoadingState
import com.ai.heymika.core.ui.component.state.MikaStateVariant
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onMaterialClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSubjectSelected = viewModel::onSubjectSelected,
        onRetry = viewModel::retry,
        onFavoriteClick = viewModel::toggleFavorite,
        onMaterialClick = onMaterialClick,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onRetry: () -> Unit,
    onFavoriteClick: (Material) -> Unit,
    onMaterialClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.home_title),
            style = HeyMikaTheme.typography.headlineLg,
            color = HeyMikaTheme.colors.textPrimary,
            modifier =
                Modifier
                    .padding(horizontal = dimens.screenMargin)
                    .padding(top = dimens.spaceMd, bottom = dimens.spaceSm)
                    .semantics { heading() },
        )
        SearchField(
            query = uiState.query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.home_search_hint),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenMargin),
        )
        SubjectFilterRow(
            subjects = uiState.subjects,
            selectedSubject = uiState.selectedSubject,
            onSubjectSelected = onSubjectSelected,
            modifier = Modifier.padding(vertical = dimens.spaceSmPlus),
        )
        when (val materials = uiState.materials) {
            is Resource.Loading -> {
                MikaLoadingState(message = stringResource(R.string.home_loading))
            }

            is Resource.Error -> {
                MikaErrorState(
                    error = materials.error,
                    onPrimary = onRetry,
                    variant = MikaStateVariant.Inline,
                    modifier = Modifier.padding(horizontal = dimens.screenMargin),
                )
            }

            is Resource.Success -> {
                val items = materials.data.orEmpty()
                if (items.isEmpty()) {
                    MikaEmptyState(
                        title = stringResource(R.string.home_empty_title),
                        body = stringResource(R.string.home_empty_body),
                        pose = MikaPose.CONFUSED,
                    )
                } else {
                    MaterialList(
                        materials = items,
                        onMaterialClick = onMaterialClick,
                        onFavoriteClick = onFavoriteClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectFilterRow(
    subjects: List<String>,
    selectedSubject: String?,
    onSubjectSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allLabel = stringResource(R.string.home_subject_all)
    val items =
        remember(subjects, allLabel) {
            listOf(FilterChipItem<String?>(key = null, label = allLabel)) +
                subjects.map { subject -> FilterChipItem<String?>(key = subject, label = subject) }
        }
    FilterChipRow(
        items = items,
        selectedKey = selectedSubject,
        onSelect = onSubjectSelected,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun MaterialList(
    materials: List<Material>,
    onMaterialClick: (String) -> Unit,
    onFavoriteClick: (Material) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = HeyMikaTheme.dimens
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = dimens.screenMargin,
                end = dimens.screenMargin,
                bottom = dimens.spaceMd,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
    ) {
        items(items = materials, key = { material -> material.id }) { material ->
            MaterialCard(
                material = material,
                onClick = { onMaterialClick(material.id) },
                onFavoriteClick = { onFavoriteClick(material) },
            )
        }
    }
}
