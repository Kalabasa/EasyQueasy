package com.leanrada.easyqueasy.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.leanrada.easyqueasy.MainActivity
import com.leanrada.easyqueasy.Permissions
import com.leanrada.easyqueasy.R

class ForegroundOverlayTileService : TileService() {
    companion object {
        fun requestListeningState(context: Context) {
            requestListeningState(context, ComponentName(context, ForegroundOverlayTileService::class.java))
        }

        @SuppressLint("WrongConstant")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun requestAddTileService(context: Context, callback: (Int) -> Unit = {}) {
            val statusBarManager = context.getSystemService(Context.STATUS_BAR_SERVICE) as StatusBarManager
            statusBarManager.requestAddTileService(
                ComponentName(context, ForegroundOverlayTileService::class.java),
                context.getString(R.string.easy_queasy),
                Icon.createWithResource(context, R.mipmap.monochrome_logo),
                context.mainExecutor,
                callback
            )
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile.state = if (ForegroundOverlayService.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
        Log.d(
            ForegroundOverlayTileService::class.simpleName,
            "Quick Settings tile listening: Updated tile, isActive: ${ForegroundOverlayService.isActive}"
        )
    }

    override fun onClick() {
        super.onClick()

        if (!Permissions.checkPermissions(this, Permissions.foregroundServicePermissions) || !Settings.canDrawOverlays(this)) {
            val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                startActivity(intent)
            }
            return
        }

        qsTile.state = if (ForegroundOverlayService.isActive) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        qsTile.updateTile()
        Log.d(
            ForegroundOverlayTileService::class.simpleName,
            "Quick Settings tile clicked: Updated tile, isActive: ${ForegroundOverlayService.isActive}"
        )

        if (ForegroundOverlayService.isActive) {
            ForegroundOverlayService.stop(this)
        } else {
            ForegroundOverlayService.start(this)
        }
    }
}

