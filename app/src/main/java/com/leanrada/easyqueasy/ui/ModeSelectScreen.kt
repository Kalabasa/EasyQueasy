package com.leanrada.easyqueasy.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
        val loaded by appData.rememberLoaded()
        val onboarded by appData.rememberOnboarded()

        if (!loaded) return@Scaffold

        ModeSelect(
            withOnboarding = !onboarded,
            onSelectDrawOverOtherApps = onSelectDrawOverOtherApps,
            onSelectAccessibilityService = onSelectAccessibilityService,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

