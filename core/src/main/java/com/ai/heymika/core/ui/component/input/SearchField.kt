package com.ai.heymika.core.ui.component.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.heymika.core.R
import com.ai.heymika.core.ui.theme.HeyMikaTheme

/**
 * A search field with the 14dp input corners of every other input: search icon, [placeholder],
 * and a clear button ("Hapus pencarian") while [query] is not empty. The keyboard shows a search
 * key that calls [onSearch].
 *
 * [trailing] adds actions after the clear button, such as a filter [ComicFieldIconButton]. Focus,
 * disabled and colours follow [ComicTextField].
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.controls_search_placeholder),
    onSearch: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    SearchFieldImpl(
        query = query,
        onQueryChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        onSearch = onSearch,
        enabled = enabled,
        trailing = trailing,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun SearchFieldImpl(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.controls_search_placeholder),
    onSearch: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    previewFocused: Boolean = false,
) {
    val clearLabel = stringResource(R.string.controls_search_clear)
    val showClear = query.isNotEmpty() && enabled
    val trailingActions: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showClear) {
                ComicFieldIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = clearLabel,
                    onClick = { onQueryChange("") },
                )
            }
            trailing?.invoke()
        }
    }
    ComicTextFieldImpl(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Outlined.Search,
        trailing = if (showClear || trailing != null) trailingActions else null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke(query) }),
        interactionSource = interactionSource,
        shape = HeyMikaTheme.shapes.input,
        previewFocused = previewFocused,
    )
}

@Preview(name = "States", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SearchFieldStatesPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchField(
                query = "",
                onQueryChange = {},
                placeholder = "Cari judul materi, topik...",
            )
            SearchFieldImpl(
                query = "Rotasi AVL",
                onQueryChange = {},
                previewFocused = true,
            )
            SearchField(
                query = "",
                onQueryChange = {},
                placeholder = "Cari set flashcard, topik pelajaran...",
                enabled = false,
            )
        }
    }
}

@Preview(name = "With trailing action", showBackground = true, backgroundColor = 0xFFF8FCFF, widthDp = 360)
@Composable
private fun SearchFieldTrailingPreview() {
    HeyMikaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchField(
                query = "",
                onQueryChange = {},
                placeholder = "Cari konsep, soal, atau kata kunci...",
                trailing = {
                    ComicFieldIconButton(
                        icon = Icons.Outlined.Tune,
                        contentDescription = "Filter dan urutkan",
                        onClick = {},
                    )
                },
            )
            SearchField(
                query = "Operasi Rotasi AVL Tree dan Kompleksitas Waktu BST",
                onQueryChange = {},
                trailing = {
                    ComicFieldIconButton(
                        icon = Icons.Outlined.Tune,
                        contentDescription = "Filter dan urutkan",
                        onClick = {},
                    )
                },
            )
        }
    }
}
