package com.leanrada.easyqueasy.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.leanrada.easyqueasy.AppDataClient
import com.leanrada.easyqueasy.R
import com.leanrada.easyqueasy.ui.Overlay

class ForegroundOverlayService : Service(), SavedStateRegistryOwner {
    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private lateinit var contentView: View

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate();
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        val appData = AppDataClient(this, lifecycleScope)

        contentView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ForegroundOverlayService)
            setViewTreeSavedStateRegistryOwner(this@ForegroundOverlayService)
            setContent {
                Overlay(appData = appData)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, startFlags: Int, startId: Int): Int {
        Log.i(this::class.simpleName, "Foreground overlay service started")

        startNotificationService()

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        try {
            windowManager.addView(contentView, layoutParams)
        } catch (ex: Exception) {
            Log.e(this::class.simpleName, "Adding overlay root view failed!", ex)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy();
        super.onDestroy()
    }

    private fun startNotificationService() {
        val channelID = "channel1"
        val channel = NotificationChannel(
            channelID,
            "Overlay notification",
            NotificationManager.IMPORTANCE_LOW
        )

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("Easy Queasy running")
            .setContentText("Tap to disable")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}
