package com.leanrada.easyqueasy

import AppDataOuterClass
import AppDataOuterClass.DrawingMode
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.leanrada.easyqueasy.Permissions.Companion.foregroundOverlayPermissionsEnsurer
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
        val ensureForegroundOverlayPermissions = foregroundOverlayPermissionsEnsurer()
        val foregroundOverlayActive = remember { mutableStateOf(false) }

        val shouldActivateForegroundOverlay = drawingMode == DrawingMode.DRAW_OVER_OTHER_APPS && foregroundOverlayActive.value
        LaunchedEffect(shouldActivateForegroundOverlay) {
            if (shouldActivateForegroundOverlay) {
                ensureForegroundOverlayPermissions {
                    startOverlayService(context)
                }
            } else {
                foregroundOverlayActive.value = false
                stopOverlayService(context)
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
                        foregroundOverlayActive = foregroundOverlayActive,
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
}

fun startOverlayService(context: Context) {
    Log.i(MainActivity::class.simpleName, "Starting foreground overlay service...")
    val intent = Intent(context, ForegroundOverlayService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

fun stopOverlayService(context: Context) {
    Log.i(MainActivity::class.simpleName, "Stopping foreground overlay service...")
    val intent = Intent(context, ForegroundOverlayService::class.java)
    context.stopService(intent)
}