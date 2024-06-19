package com.leanrada.easyqueasy

import AppDataOuterClass.DrawingMode
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.leanrada.easyqueasy.Permissions.Companion.foregroundOverlayPermissionsEnsurer
import com.leanrada.easyqueasy.services.ForegroundOverlayService
import com.leanrada.easyqueasy.ui.HomeScreen
import com.leanrada.easyqueasy.ui.ModeSelectScreen
import com.leanrada.easyqueasy.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private lateinit var appData: AppDataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appData = AppDataClient(this, lifecycleScope)
        enableEdgeToEdge()
        setContent { App() }
    }

    @Composable
    private fun App() {
        val context = LocalContext.current
        var drawingMode by appData.rememberDrawingMode()
        var onboarded by appData.rememberOnboarded()
        var onboardedAccessibilitySettings by appData.rememberOnboardedAccessibilitySettings()
        val ensureForegroundOverlayPermissions = foregroundOverlayPermissionsEnsurer()

        AppTheme(dynamicColor = false) {
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
                        onToggleOverlay = {
                            ensureForegroundOverlayPermissions {
                                startOverlayService(this)
                            }
                        },
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
    Log.i("", "Starting foreground overlay service")
    val intent = Intent(activity, ForegroundOverlayService::class.java)
    ContextCompat.startForegroundService(activity, intent)
}
