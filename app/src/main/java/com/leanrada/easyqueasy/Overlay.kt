package com.leanrada.easyqueasy

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val floatE = Math.E.toFloat()
val hexRatio = 2f * sqrt(3f) / 3f

@Composable
fun Overlay(peripherySize: Dp) {
    val sensorManager =
        ContextCompat.getSystemService(LocalContext.current, SensorManager::class.java)

    val position = remember { mutableStateListOf(0f, 0f, 0f) }
    var effectIntensity by remember { mutableFloatStateOf(0f) }

    DisposableEffect(sensorManager) {
        val accelerationListener = object : SensorEventListener {
            var lastEventTimeNanos = 0L
            val lastAcceleration = floatArrayOf(0f, 0f, 0f)
            val velocity = floatArrayOf(0f, 0f, 0f)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (lastEventTimeNanos > 0) {
                    val dt = (event.timestamp - lastEventTimeNanos) * 1e-9f

                    // smoothen acceleration sensor data
                    val accelerationSmoothFactor = 0.8f.pow(dt)
                    lastAcceleration[0] += (event.values[0] - lastAcceleration[0]) * accelerationSmoothFactor
                    lastAcceleration[1] += (event.values[1] - lastAcceleration[1]) * accelerationSmoothFactor
                    lastAcceleration[2] += (event.values[2] - lastAcceleration[2]) * accelerationSmoothFactor

                    velocity[0] -= accelerationCurve(lastAcceleration[0] * dt)
                    velocity[1] += accelerationCurve(lastAcceleration[1] * dt)
                    velocity[2] -= accelerationCurve(lastAcceleration[2] * dt)

                    position[0] += velocity[0] * dt
                    position[1] += velocity[1] * dt
                    position[2] += velocity[2] * dt

                    val accel2D = hypot(lastAcceleration[0], lastAcceleration[1])
                    val speed2D = hypot(velocity[0], velocity[1])

                    val friction = 1 / (1 + dt * (1f + 40f / (1f + accel2D * 10f)))
                    velocity[0] *= friction
                    velocity[1] *= friction
                    velocity[2] *= friction

                    effectIntensity =
                        (effectIntensity * 0.997f).pow(1.2f) + (1f - effectIntensity) * sigmoid01(
                            (speed2D - 0.12f) * 60f
                        )
                }
                lastEventTimeNanos = event.timestamp
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(
            accelerationListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        onDispose {
            sensorManager?.unregisterListener(accelerationListener)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val baseDotRadius = 4.dp.toPx() * effectIntensity
        if (baseDotRadius > 1e-12) {
            val scaledPeripherySizePx = peripherySize.toPx() * (0.2f + 0.8f * effectIntensity)

            val offsetXPx = position[0] * 1587f.dp.toPx()
            val offsetYPx = position[1] * 1587f.dp.toPx()

            val gridSizeX = 60f.dp.toPx();
            val gridSizeY = gridSizeX * hexRatio;
            for (x in -2 until (size.width / gridSizeX + 3).toInt()) {
                for (y in -1 until (size.height / gridSizeY + 1).toInt()) {
                    val pixelX = (x + 0.5f) * gridSizeX + offsetXPx % (gridSizeX * 2)
                    val pixelY = (y + 0.5f + (x % 2) * 0.5f) * gridSizeY + offsetYPx % gridSizeY
                    drawCircle(
                        color = Color.Black,
                        radius = baseDotRadius * dotRadius(
                            edgeDistance(pixelX, pixelY, size),
                            scaledPeripherySizePx
                        ),
                        center = Offset(pixelX, pixelY)
                    )
                    val whitePixelX = pixelX - gridSizeX * hexRatio * 0.5f
                    drawCircle(
                        color = Color.White,
                        radius = baseDotRadius * dotRadius(
                            edgeDistance(whitePixelX, pixelY, size),
                            scaledPeripherySizePx
                        ),
                        center = Offset(whitePixelX, pixelY)
                    )
                }
            }
        }
    }
}

fun edgeDistance(x: Float, y: Float, size: Size) =
    min(min(x, y), min(size.width - x, size.height - y))

fun dotRadius(edgeDistance: Float, peripherySize: Float) = sqrt(
    min(
        1f,
        2f * (peripherySize - edgeDistance) / peripherySize
    )
)

fun accelerationCurve(x: Float) =
    x * sigmoid01((abs(x) - 4e-12f) * 1e4f)

fun sigmoid01(x: Float) = 1f / (1f + floatE.pow(-x))
