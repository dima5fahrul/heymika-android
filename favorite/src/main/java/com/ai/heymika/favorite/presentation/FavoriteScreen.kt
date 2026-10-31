package com.ai.heymika.favorite.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.domain.model.MikaPose
import com.ai.heymika.core.ui.component.MaterialCard
import com.ai.heymika.core.ui.component.state.MikaEmptyState
import com.ai.heymika.core.ui.component.state.MikaErrorState
import com.ai.heymika.core.ui.component.state.MikaLoadingState
import com.ai.heymika.favorite.R
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful entry of the favourite screen. Resolves the [FavoriteViewModel] from the Koin module
 * the feature registered when its [com.ai.heymika.favorite.FavoriteEntry] was constructed.
 */
@Composable
fun FavoriteRoute(
    onMaterialClick: (String) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FavoriteScreen(
        state = state,
        onMaterialClick = onMaterialClick,
        onFavoriteClick = viewModel::removeFromFavorite,
        onRetry = viewModel::retry,
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    state: FavoriteUiState,
    onMaterialClick: (String) -> Unit,
    onFavoriteClick: (Material) -> Unit,
    onRetry: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.favorite_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.favorite_navigate_up),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> {
                    MikaLoadingState(message = stringResource(R.string.favorite_loading))
                }

                state.error != null -> {
                    MikaErrorState(
                        error = state.error,
                        onPrimary = onRetry,
                        onSecondary = onNavigateUp,
                    )
                }

                state.isEmpty -> {
                    MikaEmptyState(
                        title = stringResource(R.string.favorite_empty_title),
                        body = stringResource(R.string.favorite_empty_body),
                        pose = MikaPose.SLEEPING,
                    )
                }

                else -> {
                    FavoriteList(
                        materials = state.materials,
                        onMaterialClick = onMaterialClick,
                        onFavoriteClick = onFavoriteClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteList(
    materials: List<Material>,
    onMaterialClick: (String) -> Unit,
    onFavoriteClick: (Material) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = materials, key = { it.id }) { material ->
            MaterialCard(
                material = material,
                onClick = { onMaterialClick(material.id) },
                onFavoriteClick = { onFavoriteClick(material) },
            )
        }
    }
}
