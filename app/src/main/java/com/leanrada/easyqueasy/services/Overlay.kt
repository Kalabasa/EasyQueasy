package com.leanrada.easyqueasy.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.leanrada.easyqueasy.AppDataClient
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val floatE = Math.E.toFloat()
val hexRatio = 2f * sqrt(3f) / 3f

@Composable
fun Overlay(appData: AppDataClient, peripherySize: Dp) {
    val drawingMode by appData.rememberDrawingMode()
    Log.d("Overlay", "drawingMode: $drawingMode")
    val overlayAreaSize by appData.rememberOverlayAreaSize()
    Log.d("Overlay", "overlayAreaSize: $overlayAreaSize")

    val sensorManager = ContextCompat.getSystemService(LocalContext.current, SensorManager::class.java)

    val position = remember { mutableStateListOf(0f, 0f, 0f) }
    var effectIntensity by remember { mutableFloatStateOf(0f) }

    DisposableEffect(sensorManager) {
        val accelerationListener = object : SensorEventListener {
            var lastEventTimeNanos = 0L
            val velocity = floatArrayOf(0f, 0f, 0f)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (lastEventTimeNanos > 0) {
                    val dt = (event.timestamp - lastEventTimeNanos) * 1e-9f

                    velocity[0] -= accelerationCurve(event.values[0] * dt)
                    velocity[1] += accelerationCurve(event.values[1] * dt)
                    velocity[2] -= accelerationCurve(event.values[2] * dt)

                    position[0] += velocity[0] * dt
                    position[1] += velocity[1] * dt
                    position[2] += velocity[2] * dt

                    val accel2D = hypot(event.values[0], event.values[1])
                    val speed2D = hypot(velocity[0], velocity[1])

                    val friction = 1 / (1 + dt * (8f + 16f / (1f + accel2D * 60f)))
                    velocity[0] *= friction
                    velocity[1] *= friction
                    velocity[2] *= friction

                    effectIntensity *= (1f - 0.2f.pow(72f * dt))
                    effectIntensity = lerp(effectIntensity, 1f, 1f - (1f - sigmoid01((speed2D - 0.11f) * 60f)).pow(72f * dt))
                }
                lastEventTimeNanos = event.timestamp
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(
            accelerationListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST
        )
        onDispose {
            sensorManager?.unregisterListener(accelerationListener)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val baseDotRadius = 4.dp.toPx() * effectIntensity
        if (baseDotRadius > 1e-12) {
            val scaledPeripherySizePx = peripherySize.toPx() *
                    lerp(0.2f, 1f, overlayAreaSize) *
                    lerp(0.4f, 1f, effectIntensity).pow(2f)

            val offsetXPx = position[0] * 1587f.dp.toPx()
            val offsetYPx = position[1] * 1587f.dp.toPx()

            val gridSizeX = 60f.dp.toPx();
            val gridSizeY = gridSizeX / hexRatio;
            for (x in -2 until (size.width / gridSizeX + 2).toInt()) {
                for (y in -2 until (size.height / gridSizeY + 2).toInt()) {
                    val pixelX = (x + 0.5f + (y % 2) * 0.5f) * gridSizeX + offsetXPx % gridSizeX
                    val pixelY = (y + 0.5f) * gridSizeY + offsetYPx % (gridSizeY * 2)
                    drawCircle(
                        color = Color.Black,
                        radius = baseDotRadius * dotRadius(edgeDistance(pixelX, pixelY, size), scaledPeripherySizePx),
                        center = Offset(pixelX, pixelY)
                    )
                    val whitePixelY = pixelY + gridSizeY * 0.6667f
                    drawCircle(
                        color = Color.White,
                        radius = -1f + baseDotRadius * dotRadius(edgeDistance(pixelX, whitePixelY, size), scaledPeripherySizePx),
                        center = Offset(pixelX, whitePixelY)
                    )
                }
            }
        }
    }
}

fun edgeDistance(x: Float, y: Float, size: Size) = min(min(x, y), min(size.width - x, size.height - y))
fun dotRadius(edgeDistance: Float, peripherySize: Float) = sqrt(min(1f, 1.5f * (peripherySize - edgeDistance) / peripherySize))
fun accelerationCurve(x: Float) = x * sigmoid01((abs(x) - 2e-12f) * 1e4f)
fun sigmoid01(x: Float) = 1f / (1f + floatE.pow(-x))
fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t