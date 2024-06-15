package com.leanrada.easyqueasy

import AppDataOuterClass.DrawingMode
import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.leanrada.easyqueasy.services.ForegroundOverlayService
import com.leanrada.easyqueasy.ui.HomeScreen
import com.leanrada.easyqueasy.ui.ModeSelectScreen
import com.leanrada.easyqueasy.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private lateinit var appData: AppDataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appData = AppDataClient(this)
        enableEdgeToEdge()
        setContent { App() }
    }

    @Composable
    private fun App() {
        var drawingMode by appData.rememberDrawingMode()
        var onboarded by appData.rememberOnboarded()
        var onboardedAccessibilitySettings by appData.rememberOnboardedAccessibilitySettings()

        AppTheme {
            when (drawingMode) {
                DrawingMode.NONE ->
                    ModeSelectScreen(
                        appData = appData,
                        onSelectDrawOverOtherApps = {
                            drawingMode = DrawingMode.DRAW_OVER_OTHER_APPS
                            onboarded = true
                        },
                        onSelectAccessibilityService = {
                            drawingMode = DrawingMode.ACCESSIBILITY_SERVICE
                            onboarded = true
                        },
                    )

                else ->
                    HomeScreen(
                        appData = appData,
                        onToggleOverlay = { startOverlayService(this) },
                        tmp_onReset = {
                            drawingMode = DrawingMode.NONE
                            onboarded = false
                            onboardedAccessibilitySettings = false
                        }
                    )
            }
        }
    }
}

fun startOverlayService(activity: ComponentActivity) {
    Log.i("EQ", "starting ForegroundOverlayService")
    val intent = Intent(activity, ForegroundOverlayService::class.java)
    ContextCompat.startForegroundService(activity, intent)
}

@Composable
fun foregroundOverlayPermissionsEnsurer(context: Context?): (callback: () -> Unit) -> Unit {
    val savedCallback: MutableState<(() -> Unit)?> = remember { mutableStateOf(null) }

    val canDrawOverlaysLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (context != null && Settings.canDrawOverlays(context)) {
                savedCallback.value?.invoke()
                savedCallback.value = null
            }
        }

    // TODO move to Permissions.kt
    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (context != null && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.FOREGROUND_SERVICE_HEALTH
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                savedCallback.value?.invoke()
                savedCallback.value = null
            }
        }

    return { callback ->
        if (context != null) {
            if (Settings.canDrawOverlays(context) && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.FOREGROUND_SERVICE_HEALTH
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callback()
            } else {
                savedCallback.value = callback
                canDrawOverlaysLauncher.launch(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)
                    )
                )
            }
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
