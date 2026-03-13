package com.exoticstech.halo.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.exoticstech.halo.ui.theme.HaloTheme

@Composable
fun PermissionRequestDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Permissions Required")
        },
        text = {
            Text(text = "Halo needs access to your location and notifications to provide location-based alarms and alerts. Please grant these permissions to continue.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = "Grant Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Not Now")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Preview
@Composable
fun PermissionRequestDialogPreview() {
    HaloTheme {
        PermissionRequestDialog(onDismiss = {}, onConfirm = {})
    }
}
