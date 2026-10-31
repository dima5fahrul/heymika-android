package com.ai.heymika.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ai.heymika.R
import com.ai.heymika.core.domain.model.AppError
import com.ai.heymika.core.domain.model.GenerationKind
import com.ai.heymika.core.domain.model.Material
import com.ai.heymika.core.ui.component.foundation.ComicButton
import com.ai.heymika.core.ui.component.foundation.ComicButtonVariant
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.component.state.MikaErrorState
import com.ai.heymika.core.ui.component.state.MikaLoadingState
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Reader view for one [Material]: body text, a favourite toggle and the three AI actions.
 *
 * [onGenerate] does not navigate by itself — it is routed through
 * `com.ai.heymika.navigation.HeyMikaNavHost`, which installs the `:aistudio` module first when it
 * is not on the device yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    materialId: String,
    onBack: () -> Unit,
    onGenerate: (GenerationKind) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MaterialDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(materialId) { viewModel.bind(materialId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val material = uiState.material

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = material?.title.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                actions = {
                    if (material != null) {
                        FavoriteAction(
                            isFavorite = material.isFavorite,
                            onClick = viewModel::toggleFavorite,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            when {
                uiState.isLoading -> MikaLoadingState()
                material == null -> MikaErrorState(error = AppError.NotFound, onPrimary = onBack)
                else -> MaterialBody(material = material, onGenerate = onGenerate)
            }
        }
    }
}

@Composable
private fun FavoriteAction(
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
    val description =
        if (isFavorite) {
            stringResource(R.string.detail_favorite_remove)
        } else {
            stringResource(R.string.detail_favorite_add)
        }
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = description)
    }
}

@Composable
private fun MaterialBody(
    material: Material,
    onGenerate: (GenerationKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val coverUrl = material.coverUrl
        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = material.title,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large),
            )
        }
        Text(
            text = material.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text =
                stringResource(
                    R.string.detail_subject_duration,
                    material.subject,
                    material.durationMinutes,
                ),
            style = MaterialTheme.typography.labelLarge,
            color = HeyMikaTheme.colors.primaryText,
        )
        Text(
            text = material.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SubtleDivider()
        Text(
            text = material.body,
            style = MaterialTheme.typography.bodyLarge,
        )
        SubtleDivider()
        Text(
            text = stringResource(R.string.detail_generate_title),
            style = MaterialTheme.typography.titleMedium,
        )
        GenerationActions(onGenerate = onGenerate)
    }
}

@Composable
private fun GenerationActions(
    onGenerate: (GenerationKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // One Primary per region: the first action leads, the others are Outline alternatives.
        GenerationKind.entries.forEachIndexed { index, kind ->
            ComicButton(
                text = generationLabel(kind),
                onClick = { onGenerate(kind) },
                variant = if (index == 0) ComicButtonVariant.Primary else ComicButtonVariant.Outline,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun generationLabel(kind: GenerationKind): String =
    when (kind) {
        GenerationKind.QUIZ -> stringResource(R.string.action_generate_quiz)
        GenerationKind.SUMMARY -> stringResource(R.string.action_generate_summary)
        GenerationKind.STORY -> stringResource(R.string.action_generate_story)
    }
