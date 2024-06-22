package com.leanrada.easyqueasy

import AppDataOuterClass
import AppDataOuterClass.DrawingMode
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.leanrada.easyqueasy.Permissions.Companion.foregroundServicePermissions
import com.leanrada.easyqueasy.Permissions.Companion.permissionsEnsurer
import com.leanrada.easyqueasy.services.ForegroundOverlayService
import com.leanrada.easyqueasy.ui.HomeScreen
import com.leanrada.easyqueasy.ui.ModeSelectScreen
import com.leanrada.easyqueasy.ui.theme.AppTheme
import kotlinx.coroutines.launch

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
        val coroutineScope = rememberCoroutineScope()
        var drawingMode by appData.rememberDrawingMode()
        var onboarded by appData.rememberOnboarded()
        val ensureForegroundOverlayPermissions = permissionsEnsurer(foregroundServicePermissions)
        val localForegroundOverlayActive = rememberLocalForegroundOverlayActive()

        val shouldActivateForegroundOverlay = drawingMode == DrawingMode.DRAW_OVER_OTHER_APPS && localForegroundOverlayActive.value
        LaunchedEffect(shouldActivateForegroundOverlay) {
            if (shouldActivateForegroundOverlay) {
                ensureForegroundOverlayPermissions {
                    ForegroundOverlayService.start(context)
                }
            } else {
                ForegroundOverlayService.stop(context)
            }
        }

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
                        foregroundOverlayActive = localForegroundOverlayActive,
                        debug_onReset = {
                            coroutineScope.launch {
                                appData.dataStore.updateData {
                                    AppDataOuterClass.AppData.getDefaultInstance()
                                }
                            }
                        }
                    )
            }
        }
    }

    @Composable
    private fun rememberLocalForegroundOverlayActive(): MutableState<Boolean> {
        val foregroundOverlayStartTime by appData.rememberForegroundOverlayStartTime()
        val foregroundOverlayStopTime by appData.rememberForegroundOverlayStopTime()
        val foregroundOverlayActive = foregroundOverlayStartTime > foregroundOverlayStopTime
        val localForegroundOverlayActive = remember { mutableStateOf(foregroundOverlayActive) }
        LaunchedEffect(foregroundOverlayActive) {
            localForegroundOverlayActive.value = foregroundOverlayActive
        }
        return localForegroundOverlayActive
    }
}
