package com.leanrada.easyqueasy.ui

import AppDataOuterClass.DrawingMode
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.leanrada.easyqueasy.AppDataClient

@Composable
fun ModeSelectDialog(appData: AppDataClient, onDismissRequest: () -> Unit) {
    var drawingMode by appData.rememberDrawingMode()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            colors = CardDefaults.elevatedCardColors(),
            modifier = Modifier.wrapContentSize(),
        ) {
            ModeSelect(
                withOnboarding = false,
                onSelectAccessibilityService = {
                    drawingMode = DrawingMode.ACCESSIBILITY_SERVICE
                    onDismissRequest()
                },
                onSelectDrawOverOtherApps = {
                    drawingMode = DrawingMode.DRAW_OVER_OTHER_APPS
                    onDismissRequest()
                }
            )
        }
    }
}
