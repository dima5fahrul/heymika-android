package com.ai.heymika.setting.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.component.foundation.SubtleDivider
import com.ai.heymika.core.ui.component.selection.ToggleRow
import com.ai.heymika.core.ui.component.state.MikaLoadingState
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import com.ai.heymika.setting.R
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful entry of the setting screen. Resolves the [SettingViewModel] from the Koin module the
 * feature registered when its [com.ai.heymika.setting.SettingEntry] was constructed.
 */
@Composable
fun SettingRoute(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingScreen(
        state = state,
        onDailyReminderChange = viewModel::setDailyReminder,
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    state: SettingUiState,
    onDailyReminderChange: (Boolean) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.setting_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.setting_navigate_up),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                MikaLoadingState()
            }
            return@Scaffold
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionTitle(text = stringResource(R.string.setting_section_notification))
            ToggleRow(
                title = stringResource(R.string.setting_daily_reminder),
                subtitle = stringResource(R.string.setting_daily_reminder_summary),
                checked = state.dailyReminder,
                onCheckedChange = onDailyReminderChange,
            )

            SectionTitle(text = stringResource(R.string.setting_section_about))
            AboutSection()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = HeyMikaTheme.typography.overline,
        color = HeyMikaTheme.colors.primaryText,
        modifier =
            modifier
                .padding(top = 12.dp, bottom = 4.dp)
                .semantics { heading() },
    )
}

@Composable
private fun AboutSection(modifier: Modifier = Modifier) {
    ComicCard(
        modifier = modifier.fillMaxWidth(),
        variant = ComicCardVariant.Subtle,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.setting_about_app),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.setting_about_app_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SubtleDivider(modifier = Modifier.padding(vertical = 8.dp))
            AboutRow(
                label = stringResource(R.string.setting_about_version),
                value = stringResource(R.string.setting_about_version_value),
            )
            AboutRow(
                label = stringResource(R.string.setting_about_license),
                value = stringResource(R.string.setting_about_license_value),
            )
        }
    }
}

@Composable
private fun AboutRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2f),
        )
    }
}
