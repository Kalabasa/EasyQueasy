package com.leanrada.easyqueasy

import android.content.Context
import android.content.Intent
import android.provider.Settings

class Permissions {
    companion object {
        fun openAccessibilitySettings(context: Context) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }
}