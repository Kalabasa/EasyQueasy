package com.leanrada.easyqueasy.services

import android.content.Context
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

fun addOverlayView(context: Context, view: View, layoutType: Int) {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    val layoutParams = WindowManager.LayoutParams()
    layoutParams.apply {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        format = PixelFormat.TRANSPARENT
        type = layoutType
        flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    }

    windowManager.addView(view, layoutParams)
}