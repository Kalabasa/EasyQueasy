package com.leanrada.easyqueasy

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class AccessibilityOverlayService : AccessibilityService(), SavedStateRegistryOwner {
    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private lateinit var contentView: View

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate();
        super.onCreate()

        savedStateRegistryController.performRestore(null)

        contentView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@AccessibilityOverlayService)
            setViewTreeSavedStateRegistryOwner(this@AccessibilityOverlayService)
            setContent {
                Overlay(peripherySize = 120.dp)
            }
        }
    }

    @Deprecated("Deprecated in super")
    override fun onStart(intent: Intent?, startId: Int) {
        lifecycleDispatcher.onServicePreSuperOnStart();
        super.onStart(intent, startId)
    }

    override fun onServiceConnected() {
        Log.i("EQ", "AccessibilityOverlayService connected")

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
        } catch (ex: Exception) {
            Log.e("EQ", "adding view failed", ex)
        }
    }

    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy();
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