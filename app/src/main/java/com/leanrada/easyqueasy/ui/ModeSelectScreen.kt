package com.leanrada.easyqueasy.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.leanrada.easyqueasy.AppDataClient

@Composable
fun ModeSelectScreen(
    appData: AppDataClient,
    onSelectDrawOverOtherApps: () -> Unit = {},
    onSelectAccessibilityService: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        ModeSelect(
            appData = appData,
            onSelectDrawOverOtherApps = onSelectDrawOverOtherApps,
            onSelectAccessibilityService = onSelectAccessibilityService,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

