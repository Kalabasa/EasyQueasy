package com.leanrada.easyqueasy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat

class ForegroundOverlayService : Service() {
    override fun onStartCommand(intent: Intent?, startFlags: Int, startId: Int): Int {
        Log.e("EQ", "ForegroundOverlayService connected")

        startNotificationService()

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layout = LinearLayout(applicationContext)
        layout.setBackgroundColor(Color.GREEN and 0x55FFFFFF)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        try {
            windowManager.addView(layout, layoutParams)
        } catch (ex: Exception) {
            Log.e("EQ", "adding view failed", ex)
        }

        return START_STICKY
    }

    fun startNotificationService() {
        if (Build.VERSION.SDK_INT >= 26) {
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
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}