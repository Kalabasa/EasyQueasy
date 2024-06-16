package com.leanrada.easyqueasy.ui

import AppDataOuterClass.DrawingMode
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leanrada.easyqueasy.AppDataClient
import com.leanrada.easyqueasy.Permissions

@Composable
fun HomeScreen(
    appData: AppDataClient,
    onToggleOverlay: () -> Unit = {},
    tmp_onReset: () -> Unit = {}
) {
    val drawingMode by appData.rememberDrawingMode()
    val onboardedAccessibilitySettings by appData.rememberOnboardedAccessibilitySettings()

    Scaffold(
        topBar = {
            TopBar()
        },
        floatingActionButton = {
            when (drawingMode) {
                DrawingMode.DRAW_OVER_OTHER_APPS -> ToggleButton(onToggleOverlay)
                DrawingMode.ACCESSIBILITY_SERVICE -> {}
                DrawingMode.NONE -> {}
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            when (drawingMode) {
                DrawingMode.DRAW_OVER_OTHER_APPS -> {
                    GetStartedSection(DrawingMode.DRAW_OVER_OTHER_APPS)
                }

                DrawingMode.ACCESSIBILITY_SERVICE -> {
                    if (onboardedAccessibilitySettings == false) {
                        GetStartedSection(DrawingMode.ACCESSIBILITY_SERVICE)
                    }
                }

                DrawingMode.NONE -> {}
            }
            SettingsSection(appData)
            Button(onClick = tmp_onReset) {}
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Face, "")
                Spacer(Modifier.size(8.dp))
                Text(
                    "Easy Queasy",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    )
}

@Composable
fun ToggleButton(onClick: () -> Unit = {}) {
    FloatingActionButton(onClick = { onClick() }) {
        Icon(Icons.Filled.PlayArrow, "Start")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GetStartedSection(
    drawingMode: DrawingMode,
) {
    val context = LocalContext.current

    Column {
        Text(
            "Get started",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        when (drawingMode) {
            DrawingMode.DRAW_OVER_OTHER_APPS -> {
                Card(
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    GetStartedChecklistItem {
                        Text(
                            buildAnnotatedString {
                                append("Grant ")
                                appendBold("Easy Queasy")
                                append(" the permission to draw over other apps.")
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            DrawingMode.ACCESSIBILITY_SERVICE ->
                Card(
                    onClick = { Permissions.openAccessibilitySettings(context) },
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    GetStartedChecklistItem {
                        Text(
                            buildAnnotatedString {
                                append("Enable the ")
                                appendBold("Easy Queasy")
                                append(" Accessibility app and shortcut. The permission will only be used to draw over apps.")
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

            DrawingMode.NONE -> {}
        }
    }
}

@Composable
fun GetStartedChecklistItem(content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp),
    ) {
        Checkbox(checked = false, onCheckedChange = null)
        Spacer(Modifier.size(8.dp))
        content()
    }
}

@Composable
private fun SettingsSection(appData: AppDataClient) {
    var context = LocalContext.current
    var drawingMode by appData.rememberDrawingMode()

    val (overlayAreaSize, setOverlayAreaSize) = appData.rememberOverlayAreaSize()
    val overlayAreaSizeSliderState = rememberSliderState(overlayAreaSize, setOverlayAreaSize)
    Log.d("HomeScreen", "overlayAreaSize: $overlayAreaSize")

    val (overlaySpeed, setOverlaySpeed) = appData.rememberOverlaySpeed()
    val overlaySpeedSliderState = rememberSliderState(overlaySpeed, setOverlaySpeed)

    Column {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Surface(
            onClick = { Toast.makeText(context, "Color schemes not implemented yet!", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    "Color scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Black and white",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(
                "Size",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = overlayAreaSizeSliderState.value,
                onValueChange = overlayAreaSizeSliderState.onValueChange,
                valueRange = 0f..1f
            )
        }
        Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(
                "Speed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = overlaySpeedSliderState.value,
                onValueChange = overlaySpeedSliderState.onValueChange,
                valueRange = 0f..1f
            )
        }
        Surface(
            onClick = { drawingMode = DrawingMode.NONE },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    "Change mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (drawingMode) {
                        DrawingMode.DRAW_OVER_OTHER_APPS -> "Display over other apps"
                        DrawingMode.ACCESSIBILITY_SERVICE -> "Accessibility service"
                        DrawingMode.NONE -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

data class SliderState(
    val value: Float,
    val onValueChange: (Float) -> Unit,
)

@Composable
fun rememberSliderState(source: Float, setSource: (Float) -> Unit): SliderState {
    var sliderValue by remember { mutableStateOf<Float>(source) }

    return SliderState(
        sliderValue
    ) {
        sliderValue = it
        setSource(it)
    }
}