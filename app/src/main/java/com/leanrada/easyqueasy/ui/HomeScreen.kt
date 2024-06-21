package com.leanrada.easyqueasy.ui

import AppDataOuterClass.DrawingMode
import AppDataOuterClass.OverlayColor
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.leanrada.easyqueasy.AppDataClient
import com.leanrada.easyqueasy.PermissionChecker
import com.leanrada.easyqueasy.Permissions
import com.leanrada.easyqueasy.ui.theme.disabledAlpha

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
            if (permissionChecker.status == PermissionChecker.Status.OK) {
                if (drawingMode == DrawingMode.DRAW_OVER_OTHER_APPS) {
                    ToggleButton(foregroundOverlayActive)
                } else if (drawingMode == DrawingMode.ACCESSIBILITY_SERVICE) {
                    OpenAccessibilitySettingsButton()
                }
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
            SettingsSection(
                appData,
                enabled = permissionChecker.status == PermissionChecker.Status.OK,
                setPreviewMode = setPreviewMode
            )
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
            .padding(16.dp)
            .size(80.dp),
    ) {
        Icon(
            imageVector =
            if (overlayActive.value)
                Icons.Filled.Close
            else
                Icons.Filled.PlayArrow,
            contentDescription = "Toggle overlay",
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
fun OpenAccessibilitySettingsButton() {
    val context = LocalContext.current
    FloatingActionButton(
        onClick = {
            ContextCompat.startActivity(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), null)
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        modifier = Modifier
            .padding(16.dp)
            .height(80.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "",
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Open settings".uppercase(),
                style = MaterialTheme.typography.labelLarge,
            )
        }
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
private fun SettingsSection(appData: AppDataClient, enabled: Boolean = false, setPreviewMode: (value: PreviewMode) -> Unit = {}) {
    val context = LocalContext.current
    val drawingMode by appData.rememberDrawingMode()
    val alphaForEnabled = if (enabled) 1f else disabledAlpha

    var overlayColor by appData.rememberOverlayColor()

    val (overlayAreaSize, setOverlayAreaSize) = appData.rememberOverlayAreaSize()
    val overlayAreaSizeSliderState = rememberSliderState(overlayAreaSize, setOverlayAreaSize)

    val (overlaySpeed, setOverlaySpeed) = appData.rememberOverlaySpeed()
    val overlaySpeedSliderState = rememberSliderState(overlaySpeed, setOverlaySpeed)

    Column {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .alpha(alphaForEnabled)
        )

        var colorSchemeDialogActive by remember { mutableStateOf(false) }

        Surface(
            onClick = { colorSchemeDialogActive = true },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alphaForEnabled),
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    "Color scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    overlayColorLabel(overlayColor),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (colorSchemeDialogActive) {
            Dialog(onDismissRequest = { colorSchemeDialogActive = false }) {
                Card(colors = CardDefaults.elevatedCardColors()) {
                    OverlayColor.values().forEach {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    overlayColor = it
                                    colorSchemeDialogActive = false
                                }
                                .padding(16.dp)
                        ) {
                            Text(overlayColorLabel(it), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        Column(
            Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .alpha(alphaForEnabled)
        ) {
            Text(
                "Size",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                enabled = enabled,
                value = overlayAreaSizeSliderState.value,
                onValueChange = {
                    overlayAreaSizeSliderState.onValueChange(it)
                    setPreviewMode(PreviewMode.SIZE)
                },
                onValueChangeFinished = { setPreviewMode(PreviewMode.NONE) },
                valueRange = 0f..1f,
            )
        }

        Column(
            Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .alpha(alphaForEnabled)
        ) {
            Text(
                "Speed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Slider(
                enabled = enabled,
                value = overlaySpeedSliderState.value,
                onValueChange = {
                    overlaySpeedSliderState.onValueChange(it)
                    setPreviewMode(PreviewMode.SPEED)
                },
                onValueChangeFinished = { setPreviewMode(PreviewMode.NONE) },
                valueRange = 0f..1f,
            )
        }

        var modeSelectDialogActive by remember { mutableStateOf(false) }

        Surface(
            onClick = { modeSelectDialogActive = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    "Mode",
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

        if (modeSelectDialogActive) {
            ModeSelectDialog(appData = appData) {
                modeSelectDialogActive = false
            }
        }
    }
}

private fun overlayColorLabel(overlayColor: OverlayColor) = when (overlayColor) {
    OverlayColor.BLACK_AND_WHITE -> "Black and white"
    OverlayColor.BLACK -> "Black"
    OverlayColor.WHITE -> "White"
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