package com.ai.heymika.debug.gallery

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ai.heymika.HeyMikaRootFrame
import com.ai.heymika.R
import com.ai.heymika.core.domain.connectivity.ConnectivityObserver
import com.ai.heymika.core.ui.component.display.InfoNoteCard
import com.ai.heymika.core.ui.component.display.SectionHeader
import com.ai.heymika.core.ui.component.foundation.ComicCard
import com.ai.heymika.core.ui.component.foundation.ComicCardVariant
import com.ai.heymika.core.ui.component.navigation.HeyMikaLargeTopBar
import com.ai.heymika.core.ui.component.selection.ToggleRow
import com.ai.heymika.core.ui.theme.HeyMikaTheme
import org.koin.compose.koinInject

/** Every gallery page, reachable with `--es section <id>`. */
enum class GallerySection(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
) {
    TOKENS("tokens", R.string.gallery_section_tokens, R.string.gallery_section_tokens_body),
    TONES("tones", R.string.gallery_section_tones, R.string.gallery_section_tones_body),
    SURFACES("surfaces", R.string.gallery_section_surfaces, R.string.gallery_section_surfaces_body),
    SURFACES_MORE(
        "surfaces_more",
        R.string.gallery_section_surfaces_more,
        R.string.gallery_section_surfaces_more_body,
    ),
    TYPOGRAPHY("typography", R.string.gallery_section_typography, R.string.gallery_section_typography_body),
    BUTTONS("buttons", R.string.gallery_section_buttons, R.string.gallery_section_buttons_body),
    BUTTONS_MORE("buttons_more", R.string.gallery_section_buttons_more, R.string.gallery_section_buttons_more_body),
    INPUTS("inputs", R.string.gallery_section_inputs, R.string.gallery_section_inputs_body),
    INPUTS_MORE("inputs_more", R.string.gallery_section_inputs_more, R.string.gallery_section_inputs_more_body),
    SELECTION("selection", R.string.gallery_section_selection, R.string.gallery_section_selection_body),
    SELECTION_MORE(
        "selection_more",
        R.string.gallery_section_selection_more,
        R.string.gallery_section_selection_more_body,
    ),
    DISPLAY("display", R.string.gallery_section_display, R.string.gallery_section_display_body),
    DISPLAY_DATA("display_data", R.string.gallery_section_display_data, R.string.gallery_section_display_data_body),
    DISPLAY_FEEDBACK(
        "display_feedback",
        R.string.gallery_section_display_feedback,
        R.string.gallery_section_display_feedback_body,
    ),
    NAVIGATION("navigation", R.string.gallery_section_navigation, R.string.gallery_section_navigation_body),
    OVERLAYS("overlays", R.string.gallery_section_overlays, R.string.gallery_section_overlays_body),
    PICKERS("pickers", R.string.gallery_section_pickers, R.string.gallery_section_pickers_body),
    MASCOT("mascot", R.string.gallery_section_mascot, R.string.gallery_section_mascot_body),
    MASCOT_SPEECH("mascot_speech", R.string.gallery_section_mascot_speech, R.string.gallery_section_mascot_speech_body),
    STATES("states", R.string.gallery_section_states, R.string.gallery_section_states_body),
    STATES_INLINE("states_inline", R.string.gallery_section_states_inline, R.string.gallery_section_states_inline_body),
    ;

    companion object {
        fun fromId(id: String?): GallerySection? = entries.firstOrNull { it.id == id }
    }
}

@Composable
fun DesignGalleryApp(
    request: GalleryRequest,
    onExit: () -> Unit,
) {
    var current by rememberSaveable(request) { mutableStateOf(GallerySection.fromId(request.section)) }
    var forcedOffline by rememberSaveable(request) { mutableStateOf(request.offline) }
    val realObserver: ConnectivityObserver = koinInject()
    val observer = remember(realObserver) { GalleryConnectivity(realObserver) }
    LaunchedEffect(forcedOffline) { observer.forcedOffline.value = forcedOffline }

    HeyMikaRootFrame(connectivityObserver = observer) {
        val section = current
        if (section == null) {
            BackHandler(onBack = onExit)
            GalleryIndex(
                forcedOffline = forcedOffline,
                onForcedOfflineChange = { forcedOffline = it },
                onOpen = { current = it },
            )
        } else {
            val back = { current = null }
            BackHandler(onBack = back)
            GallerySectionContent(section = section, request = request, onBack = back)
        }
    }
}

@Composable
private fun GallerySectionContent(
    section: GallerySection,
    request: GalleryRequest,
    onBack: () -> Unit,
) {
    val title = stringResource(section.titleRes)
    when (section) {
        GallerySection.TOKENS -> TokensPage(title, onBack)
        GallerySection.TONES -> TonesPage(title, onBack)
        GallerySection.SURFACES -> SurfacesPage(title, onBack)
        GallerySection.SURFACES_MORE -> SurfacesMorePage(title, onBack)
        GallerySection.TYPOGRAPHY -> TypographyPage(title, onBack)
        GallerySection.BUTTONS -> ButtonsPage(title, onBack)
        GallerySection.BUTTONS_MORE -> ButtonsMorePage(title, onBack)
        GallerySection.INPUTS -> InputsPage(title, onBack)
        GallerySection.INPUTS_MORE -> InputsMorePage(title, onBack)
        GallerySection.SELECTION -> SelectionPage(title, onBack)
        GallerySection.SELECTION_MORE -> SelectionMorePage(title, onBack)
        GallerySection.DISPLAY -> DisplayPage(title, onBack)
        GallerySection.DISPLAY_DATA -> DisplayDataPage(title, onBack)
        GallerySection.DISPLAY_FEEDBACK -> DisplayFeedbackPage(title, onBack)
        GallerySection.NAVIGATION -> NavigationPage(title, onBack)
        GallerySection.OVERLAYS -> OverlaysPage(title, onBack, open = request.open)
        GallerySection.PICKERS -> PickersPage(title, onBack, open = request.open)
        GallerySection.MASCOT -> MascotPage(title, onBack)
        GallerySection.MASCOT_SPEECH -> MascotSpeechPage(title, onBack)
        GallerySection.STATES -> StatesPage(title, onBack, initialState = request.state)
        GallerySection.STATES_INLINE -> StatesInlinePage(title, onBack, initialState = request.state)
    }
}

@Composable
private fun GalleryIndex(
    forcedOffline: Boolean,
    onForcedOfflineChange: (Boolean) -> Unit,
    onOpen: (GallerySection) -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        HeyMikaLargeTopBar(
            title = stringResource(R.string.gallery_title),
            subtitle = stringResource(R.string.gallery_subtitle),
        )
        Column(
            modifier = Modifier.padding(horizontal = dimens.screenMargin, vertical = dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceSmPlus),
        ) {
            InfoNoteCard(
                title = stringResource(R.string.gallery_decision_title),
                body = stringResource(R.string.gallery_decision_body),
            )
            ToggleRow(
                title = stringResource(R.string.gallery_simulate_offline),
                subtitle = stringResource(R.string.gallery_simulate_offline_body),
                checked = forcedOffline,
                onCheckedChange = onForcedOfflineChange,
            )
            SectionHeader(title = stringResource(R.string.gallery_sections), compact = true)
            GallerySection.entries.forEach { section ->
                SectionRow(section = section, onClick = { onOpen(section) })
            }
        }
    }
}

@Composable
private fun SectionRow(
    section: GallerySection,
    onClick: () -> Unit,
) {
    val dimens = HeyMikaTheme.dimens
    val title = stringResource(section.titleRes)
    ComicCard(
        modifier = Modifier.fillMaxWidth(),
        variant = ComicCardVariant.Subtle,
        onClick = onClick,
        onClickLabel = stringResource(R.string.gallery_open_section, title),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = HeyMikaTheme.typography.labelLg, color = HeyMikaTheme.colors.textPrimary)
                Text(
                    text = stringResource(section.bodyRes) + " · " + section.id,
                    style = HeyMikaTheme.typography.caption,
                    color = HeyMikaTheme.colors.textMuted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = HeyMikaTheme.colors.textMuted,
                modifier = Modifier.padding(start = dimens.spaceSm),
            )
        }
    }
}
