package com.leanrada.easyqueasy.ui

import AppDataOuterClass.DrawingMode
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leanrada.easyqueasy.AppDataClient
import com.leanrada.easyqueasy.PermissionChecker
import com.leanrada.easyqueasy.Permissions

@Composable
fun HomeScreen(
    appData: AppDataClient,
    foregroundOverlayActive: MutableState<Boolean>,
    debug_onReset: () -> Unit = {}
) {
    val permissionChecker by Permissions.rememberPermissionChecker(appData)
    val loaded by appData.rememberLoaded()
    val drawingMode by appData.rememberDrawingMode()
    val (previewMode, setPreviewMode) = remember { mutableStateOf(PreviewMode.NONE) }

    Scaffold(
        topBar = {
            TopBar(onLongPressIcon = debug_onReset)
        },
        floatingActionButton = {
            if (drawingMode == DrawingMode.DRAW_OVER_OTHER_APPS && permissionChecker.status == PermissionChecker.Status.OK) {
                ToggleButton(foregroundOverlayActive)
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        if (!loaded) return@Scaffold

        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            GetStartedSection(permissionChecker)
            SettingsSection(appData, setPreviewMode)
        }
    }

    if (previewMode != PreviewMode.NONE) {
        Overlay(
            appData = appData,
            previewMode = previewMode,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBar(onLongPressIcon: () -> Unit = {}) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🍋",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .pointerInput(onLongPressIcon) {
                            detectTapGestures(
                                onLongPress = { onLongPressIcon() }
                            )
                        },
                )
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
fun ToggleButton(overlayActive: MutableState<Boolean>) {
    FloatingActionButton(
        onClick = { overlayActive.value = !overlayActive.value },
        shape = RoundedCornerShape(24.dp),
        containerColor =
        if (overlayActive.value)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.primaryContainer,
        contentColor =
        if (overlayActive.value)
            MaterialTheme.colorScheme.onSecondaryContainer
        else
            MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .size((80 + 32).dp)
            .padding(16.dp),
    ) {
        Icon(
            imageVector =
            if (overlayActive.value)
                Icons.Filled.Close
            else
                Icons.Filled.PlayArrow,
            contentDescription = "Start",
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun GetStartedSection(permissionChecker: PermissionChecker) {
    if (permissionChecker.status == PermissionChecker.Status.OK) return

    Column(Modifier.padding(bottom = 16.dp)) {
        Text(
            "Get started",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        when (permissionChecker.status) {
            PermissionChecker.Status.REQUEST_DRAW_OVERLAY_PERMISSION -> {
                GetStartedCard(
                    onClick = { permissionChecker.request {} }
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

            PermissionChecker.Status.REQUEST_ACCESSIBILITY_PERMISSION ->
                GetStartedCard(
                    onClick = { permissionChecker.request {} }
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

            else -> {}
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GetStartedCard(onClick: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        content()
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
private fun SettingsSection(appData: AppDataClient, setPreviewMode: (value: PreviewMode) -> Unit = {}) {
    val context = LocalContext.current
    var drawingMode by appData.rememberDrawingMode()

    val (overlayAreaSize, setOverlayAreaSize) = appData.rememberOverlayAreaSize()
    val overlayAreaSizeSliderState = rememberSliderState(overlayAreaSize, setOverlayAreaSize)

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
                onValueChange = {
                    overlayAreaSizeSliderState.onValueChange(it)
                    setPreviewMode(PreviewMode.SIZE)
                },
                onValueChangeFinished = { setPreviewMode(PreviewMode.NONE) },
                valueRange = 0f..1f,
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
                onValueChange = {
                    overlaySpeedSliderState.onValueChange(it)
                    setPreviewMode(PreviewMode.SPEED)
                },
                onValueChangeFinished = { setPreviewMode(PreviewMode.NONE) },
                valueRange = 0f..1f,
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
    val sliderValue by rememberUpdatedState(source)

    return SliderState(
        sliderValue
    ) {
        setSource(it)
    }
}