package com.leanrada.easyqueasy

import AppDataOuterClass.DrawingMode
import android.Manifest.permission.FOREGROUND_SERVICE
import android.Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.leanrada.easyqueasy.PermissionChecker.Status

class PermissionChecker(
    val status: Status,
    val request: (callback: () -> Unit) -> Unit
) {
    enum class Status {
        REQUEST_ACCESSIBILITY_PERMISSION,
        REQUEST_DRAW_OVERLAY_PERMISSION,
        OK,
    }
}

class Permissions {
    companion object {
        @Composable
        fun rememberPermissionChecker(appData: AppDataClient): State<PermissionChecker> {
            val context = LocalContext.current
            val drawingMode by appData.rememberDrawingMode()
            val onboardedAccessibilitySettings by appData.rememberOnboardedAccessibilitySettings()

            var invalidateCounter by remember { mutableIntStateOf(0) }
            var savedCallback by remember { mutableStateOf({}) }

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (checkPermissions(context, drawingMode, onboardedAccessibilitySettings) == Status.OK) {
                    savedCallback.invoke()
                    savedCallback = {}
                }
                invalidateCounter++
            }

            return object : State<PermissionChecker> {
                override val value: PermissionChecker
                    get() {
                        val status = checkPermissions(context, drawingMode, onboardedAccessibilitySettings)
                        return PermissionChecker(status) {
                            launcher.launch(
                                when (status) {
                                    Status.REQUEST_ACCESSIBILITY_PERMISSION -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

                                    Status.REQUEST_DRAW_OVERLAY_PERMISSION -> Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                    )

                                    Status.OK -> Intent()
                                }
                            )
                        }
                    }
            }
        }

        private fun checkPermissions(
            context: Context,
            drawingMode: DrawingMode,
            onboardedAccessibilitySettings: Boolean
        ): Status {
            return when (drawingMode) {
                DrawingMode.DRAW_OVER_OTHER_APPS ->
                    if (Settings.canDrawOverlays(context))
                        Status.OK
                    else
                        Status.REQUEST_DRAW_OVERLAY_PERMISSION

                DrawingMode.ACCESSIBILITY_SERVICE ->
                    if (onboardedAccessibilitySettings)
                        Status.OK
                    else
                        Status.REQUEST_ACCESSIBILITY_PERMISSION

                DrawingMode.NONE -> Status.OK
            }
        }

        @Composable
        fun foregroundOverlayPermissionsEnsurer(): (callback: () -> Unit) -> Unit {
            val context = LocalContext.current

            val savedCallback: MutableState<(() -> Unit)?> = remember { mutableStateOf(null) }

            val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (hasForegroundServicePermissions(context)) {
                    savedCallback.value?.invoke()
                    savedCallback.value = null
                }
            }

            return { callback ->
                if (hasForegroundServicePermissions(context)) {
                    callback()
                } else {
                    savedCallback.value = callback
                    permissionsLauncher.launch(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            arrayOf(SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            arrayOf(SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE)
                        } else {
                            arrayOf(SYSTEM_ALERT_WINDOW)
                        }
                    )
                }
            }
        }

        private fun hasForegroundServicePermissions(context: Context): Boolean {
            if (ContextCompat.checkSelfPermission(context, FOREGROUND_SERVICE) != PERMISSION_GRANTED) {
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(context, FOREGROUND_SERVICE_SPECIAL_USE) != PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }
    }
}
