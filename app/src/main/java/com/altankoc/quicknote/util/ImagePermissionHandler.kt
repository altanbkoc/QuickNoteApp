package com.altankoc.quicknote.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class ImagePermissionHandler {
    companion object {
        fun getRequiredPermissions(): List<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        fun hasImagePermission(context: Context): Boolean {
            val permissions = getRequiredPermissions()
            return permissions.any { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun showPermissionDeniedToast(context: Context) {
            Toast.makeText(
                context,
                "Gallery permission is required to add images to notes",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberImagePermissionState(
    onPermissionResult: (Boolean) -> Unit
): ImagePermissionComposableState {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission) { isGranted ->
        onPermissionResult(isGranted)
        if (!isGranted) {
            ImagePermissionHandler.showPermissionDeniedToast(context)
        }
    }

    var showRationale by remember { mutableStateOf(false) }
    var permissionDeniedCount by remember { mutableStateOf(0) }

    return ImagePermissionComposableState(
        hasPermission = permissionState.status == PermissionStatus.Granted,
        shouldShowRationale = permissionState.status.shouldShowRationale,
        isPermissionDenied = permissionState.status is PermissionStatus.Denied,
        showRationale = showRationale,
        requestPermission = {
            when {
                permissionState.status == PermissionStatus.Granted -> {
                    onPermissionResult(true)
                }
                permissionState.status.shouldShowRationale -> {
                    showRationale = true
                }
                else -> {
                    permissionState.launchPermissionRequest()
                }
            }
        },
        onRationaleDismissed = {
            showRationale = false
            if (permissionDeniedCount >= 1) {
                ImagePermissionHandler.showPermissionDeniedToast(context)
            } else {
                permissionState.launchPermissionRequest()
                permissionDeniedCount++
            }
        }
    )
}

data class ImagePermissionComposableState(
    val hasPermission: Boolean,
    val shouldShowRationale: Boolean,
    val isPermissionDenied: Boolean,
    val showRationale: Boolean,
    val requestPermission: () -> Unit,
    val onRationaleDismissed: () -> Unit
)