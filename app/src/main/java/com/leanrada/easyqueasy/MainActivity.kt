package com.leanrada.easyqueasy

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.leanrada.easyqueasy.ui.theme.EasyQueasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            EasyQueasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = { ToggleButton() }) { innerPadding ->
                    Text(
                        text = "Hello from MainActivity",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleButton() {
    val activity = LocalContext.current.getActivity()
    FloatingActionButton(onClick = {
    }) {
        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Start")
    }
}

fun startOverlayService(activity: ComponentActivity) {
    Log.i("EQ", "starting ForegroundOverlayService")
    val intent = Intent(activity, ForegroundOverlayService::class.java)
    ContextCompat.startForegroundService(activity, intent)
}

@Composable
fun foregroundOverlayPermissionsEnsurer(context: Context?): (callback: () -> Unit) -> Unit {
    val savedCallback: MutableState<(() -> Unit)?> = remember { mutableStateOf(null) }

    val canDrawOverlaysLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (context != null && Settings.canDrawOverlays(context)) {
                savedCallback.value?.invoke()
                savedCallback.value = null
            }
        }

    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (context != null && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.FOREGROUND_SERVICE_HEALTH
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                savedCallback.value?.invoke()
                savedCallback.value = null
            }
        }

    return { callback ->
        if (context != null) {
            if (Settings.canDrawOverlays(context) && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.FOREGROUND_SERVICE_HEALTH
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callback()
            } else {
                savedCallback.value = callback
                canDrawOverlaysLauncher.launch(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)
                    )
                )
            }
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
