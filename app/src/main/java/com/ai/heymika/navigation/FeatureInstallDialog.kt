package com.ai.heymika.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ai.heymika.R

/**
 * Progress UI for an on-demand module. Shown instead of navigating when the user taps a tab or
 * an action whose module is not on the device yet, so nothing ever crashes with a missing
 * destination.
 */
@Composable
fun FeatureInstallDialog(
    featureLabel: String,
    state: FeatureInstallState,
    onRetry: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = featureLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = installMessage(state))
                when (state) {
                    is FeatureInstallState.Downloading -> {
                        LinearProgressIndicator(
                            progress = { state.fraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    FeatureInstallState.Pending,
                    FeatureInstallState.Installing,
                    -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    else -> {
                        Unit
                    }
                }
            }
        },
        confirmButton = {
            val retryable =
                state is FeatureInstallState.Failed ||
                    state is FeatureInstallState.NotInstalled
            when {
                state is FeatureInstallState.RequiresConfirmation -> {
                    TextButton(onClick = onConfirm) {
                        Text(text = stringResource(R.string.action_confirm))
                    }
                }

                retryable -> {
                    TextButton(onClick = onRetry) {
                        Text(text = stringResource(R.string.action_retry))
                    }
                }

                else -> {
                    Unit
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun installMessage(state: FeatureInstallState): String =
    when (state) {
        FeatureInstallState.NotInstalled -> {
            stringResource(R.string.feature_install_not_installed)
        }

        FeatureInstallState.Pending -> {
            stringResource(R.string.feature_install_pending)
        }

        is FeatureInstallState.Downloading -> {
            stringResource(
                R.string.feature_install_downloading,
                (state.fraction * 100).toInt(),
            )
        }

        FeatureInstallState.Installing -> {
            stringResource(R.string.feature_install_installing)
        }

        FeatureInstallState.Installed -> {
            stringResource(R.string.feature_install_installed)
        }

        is FeatureInstallState.RequiresConfirmation -> {
            stringResource(R.string.feature_install_confirm)
        }

        is FeatureInstallState.Failed -> {
            when (state.reason) {
                FeatureInstallState.FailureReason.START_REJECTED -> {
                    stringResource(R.string.feature_install_failed_start)
                }

                FeatureInstallState.FailureReason.PLAY_ERROR -> {
                    stringResource(R.string.feature_install_failed_play, state.errorCode ?: 0)
                }

                FeatureInstallState.FailureReason.ENTRY_UNAVAILABLE -> {
                    stringResource(R.string.feature_install_failed_entry)
                }
            }
        }
    }
