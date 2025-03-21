package com.voximplant.demos.kitchat.ui

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.voximplant.demos.kitchat.R
import com.voximplant.demos.kitchat.ui.theme.KitChatDemoTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsPermissionEffect(
    notificationsPermissionRequest: Boolean,
    notificationsPermissionGranted: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val lifecycleOwner = LocalLifecycleOwner.current

    val notificationsPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS,
        onPermissionResult = notificationsPermissionGranted,
    )

    LaunchedEffect(Unit) {
        notificationsPermissionState.launchPermissionRequest()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsPermissionState.status.isGranted.let { isGranted ->
                    notificationsPermissionGranted(isGranted)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (notificationsPermissionRequest) {
        NotificationsSettingsDialog(
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun NotificationsSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
            ) {
                Text(text = stringResource(id = R.string.settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = { Text(text = stringResource(R.string.permission_not_granted)) },
        text = { Text(text = stringResource(R.string.allow_notification)) },
    )
}

@Preview
@Composable
private fun SettingsDialogPreview() {
    KitChatDemoTheme {
        NotificationsSettingsDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

