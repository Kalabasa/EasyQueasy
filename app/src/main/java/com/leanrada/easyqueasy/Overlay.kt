package com.leanrada.easyqueasy

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.FloatMath
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun Overlay(peripherySize: Dp) {
    val sensorManager =
        ContextCompat.getSystemService(LocalContext.current, SensorManager::class.java)

    val positionEstimate = remember { mutableStateListOf(0f, 0f, 0f) }
    val velocityEstimate = remember { mutableStateListOf(0f, 0f, 0f) }

    DisposableEffect(sensorManager) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                velocityEstimate[0] -= accelerationCurve(event.values[0])
                velocityEstimate[1] += accelerationCurve(event.values[1])
                velocityEstimate[2] -= accelerationCurve(event.values[2])
                // assume this listener fires regularly at the specified interval
                positionEstimate[0] += velocityEstimate[0] * SensorManager.SENSOR_DELAY_GAME
                positionEstimate[1] += velocityEstimate[1] * SensorManager.SENSOR_DELAY_GAME
                positionEstimate[2] += velocityEstimate[2] * SensorManager.SENSOR_DELAY_GAME
                velocityEstimate[0] *= 0.98f
                velocityEstimate[1] *= 0.98f
                velocityEstimate[2] *= 0.98f
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(
            listener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_GAME
        )
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val peripherySizePx = peripherySize.toPx()
        val offsetXPx = positionEstimate[0] * 0.5f.dp.toPx()
        val offsetYPx = positionEstimate[1] * 0.5f.dp.toPx()
        Log.d("EQ", "offset: $offsetXPx, $offsetXPx")
        val gridSizeX = 40f.dp.toPx();
        val gridSizeY = gridSizeX * 1.3333f;
        for (x in -2 until (size.width / gridSizeX + 2).toInt()) {
            for (y in 0 until (size.height / gridSizeY + 1).toInt()) {
                val pixelX = (x + 0.5f) * gridSizeX + offsetXPx % (gridSizeX * 2)
                val pixelY = (y + 0.5f + (x % 2) * 0.5f) * gridSizeY + offsetYPx % gridSizeY
                val edgeDistance =
                    min(min(pixelX, pixelY), min(size.width - pixelX, size.height - pixelY))
                val radius =
                    4.dp.toPx() * sqrt(min(1f, (peripherySizePx - edgeDistance) / peripherySizePx))
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = Offset(pixelX, pixelY)
                )
                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = Offset(pixelX - gridSizeX * 0.6667f, pixelY)
                )
            }
        }
    }
}

val floatE = Math.E.toFloat()

fun accelerationCurve(x: Float) = x / (1 + floatE.pow(abs(x) / 6f - 0.05f))