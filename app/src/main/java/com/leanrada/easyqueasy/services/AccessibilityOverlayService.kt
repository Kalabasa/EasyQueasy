package com.leanrada.easyqueasy.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.leanrada.easyqueasy.AppDataClient
import com.leanrada.easyqueasy.ui.Overlay

class AccessibilityOverlayService : AccessibilityService(), SavedStateRegistryOwner {
    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private lateinit var contentView: View

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        val appData = AppDataClient(this, lifecycleScope)

        contentView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@AccessibilityOverlayService)
            setViewTreeSavedStateRegistryOwner(this@AccessibilityOverlayService)
            setContent {
                var onboardedAccessibilitySettings by appData.rememberOnboardedAccessibilitySettings()

                LaunchedEffect(onboardedAccessibilitySettings) {
                    if (!onboardedAccessibilitySettings) {
                        onboardedAccessibilitySettings = true
                    }
                }

                Overlay(appData = appData)
            }
        }
    }

    @Deprecated("Deprecated in super")
    override fun onStart(intent: Intent?, startId: Int) {
        lifecycleDispatcher.onServicePreSuperOnStart();
        @Suppress("DEPRECATION")
        super.onStart(intent, startId)
    }

    override fun onServiceConnected() {
        Log.i(AccessibilityOverlayService::class.simpleName, "Accessibility overlay service connected")

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        try {
            windowManager.addView(contentView, layoutParams)
        } catch (e: Exception) {
            Log.e(AccessibilityOverlayService::class.simpleName, "Adding overlay root view failed!", e)
        }
    }

    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}