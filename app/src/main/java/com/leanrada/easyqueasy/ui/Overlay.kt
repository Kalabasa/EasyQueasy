package com.leanrada.easyqueasy.ui

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import com.leanrada.easyqueasy.AppDataClient
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val floatE = Math.E.toFloat()
val hexRatio = 2f * sqrt(3f) / 3f
const val startEffectDurationMillis = 800L

@Composable
fun Overlay(appData: AppDataClient, peripherySize: Dp = 180.dp) {
    val configuration = LocalConfiguration.current

    val overlayAreaSize by appData.rememberOverlayAreaSize()
    val overlaySpeed by appData.rememberOverlaySpeed()

    val startTimeMillis by remember {
        object : State<Long> {
            override val value: Long = System.currentTimeMillis()
        }
    }

    val requestingLivePreview = false

    val position = remember { mutableStateListOf(0f, 0f, 0f) }
    var effectIntensity by remember { mutableFloatStateOf(0f) }

    val sensorManager = ContextCompat.getSystemService(LocalContext.current, SensorManager::class.java)

    DisposableEffect(sensorManager) {
        val accelerationListener = object : SensorEventListener {
            var lastEventTimeNanos = 0L
            val velocity = floatArrayOf(0f, 0f, 0f)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (lastEventTimeNanos > 0) {
                    val dt = (event.timestamp - lastEventTimeNanos) * 1e-9f

                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    val accelerationX = event.values[if (isPortrait) 0 else 1]
                    val accelerationY = event.values[if (isPortrait) 1 else 0]

                    velocity[0] -= accelerationCurve(accelerationX * dt)
                    velocity[1] += accelerationCurve(accelerationY * dt)
                    velocity[2] -= accelerationCurve(event.values[2] * dt)

                    position[0] += velocity[0] * dt
                    position[1] += velocity[1] * dt
                    position[2] += velocity[2] * dt

                    val accel2D = hypot(accelerationX, accelerationY)
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
        val previewEffectIntensity = if (requestingLivePreview) 1f else effectIntensity

        val startupEffectProgress = (System.currentTimeMillis() - startTimeMillis).toFloat() / startEffectDurationMillis
        val startupEffectActive = !requestingLivePreview && startupEffectProgress in 0f..1f

        val baseDotRadius = 4.dp.toPx() * previewEffectIntensity

        if (baseDotRadius > 0.15f || startupEffectActive) {
            val scaledPeripherySizePx = peripherySize.toPx() *
                    lerp(0.2f, 1f, overlayAreaSize) *
                    lerp(0.4f, 1f, previewEffectIntensity).pow(2f)

            val speedFactor = if (requestingLivePreview) 0f else lerp(0.2f, 2f, overlaySpeed)
            val offsetXPx = position[0] * 1587f.dp.toPx() * speedFactor
            val offsetYPx = position[1] * 1587f.dp.toPx() * speedFactor

            val gridSizeX = 60f.dp.toPx();
            val gridSizeY = gridSizeX / hexRatio;
            for (x in -2 until (size.width / gridSizeX + 2).toInt()) {
                for (y in -2 until (size.height / gridSizeY + 2).toInt()) {
                    val pixelX = (x + 0.5f + (y % 2) * 0.5f) * gridSizeX + offsetXPx % gridSizeX
                    val pixelY = (y + 0.5f) * gridSizeY + offsetYPx % (gridSizeY * 2)
                    drawCircle(
                        color = Color.Black,
                        radius = dotRadius(
                            baseDotRadius,
                            pixelX,
                            pixelY,
                            size,
                            scaledPeripherySizePx,
                            startupEffectActive,
                            startupEffectProgress
                        ),
                        center = Offset(pixelX, pixelY)
                    )
                    val whitePixelY = pixelY + gridSizeY * 0.6667f
                    drawCircle(
                        color = Color.White,
                        radius = -1f + dotRadius(
                            baseDotRadius,
                            pixelX,
                            whitePixelY,
                            size,
                            scaledPeripherySizePx,
                            startupEffectActive,
                            startupEffectProgress
                        ),
                        center = Offset(pixelX, whitePixelY)
                    )
                }
            }
        }
    }
}

private fun dotRadius(
    baseDotRadius: Float,
    x: Float,
    y: Float,
    screenSize: Size,
    scaledPeripherySizePx: Float,
    startupEffectActive: Boolean,
    startupEffectProgress: Float
) = (max(0f, baseDotRadius * dotRadiusFactor(edgeDistance(x, y, screenSize), scaledPeripherySizePx))
        + startUpEffectRadius(startupEffectActive, startupEffectProgress, y, screenSize))

private fun edgeDistance(x: Float, y: Float, screenSize: Size) = min(min(x, y), min(screenSize.width - x, screenSize.height - y))
private fun dotRadiusFactor(edgeDistance: Float, peripherySize: Float) =
    sqrt(MathUtils.clamp(1.5f * (peripherySize - edgeDistance) / peripherySize, 0f, 1f))

private fun startUpEffectRadius(startupEffectActive: Boolean, startupEffectProgress: Float, y: Float, screenSize: Size): Float =
    if (startupEffectActive)
        40f * max(0f, 0.3f - abs((1f - y / screenSize.height) - lerp(-0.3f, 1.3f, startupEffectProgress.pow(1.3f))))
    else
        0f

private fun accelerationCurve(x: Float) = x * sigmoid01((abs(x) - 2e-12f) * 1e4f)
private fun sigmoid01(x: Float) = 1f / (1f + floatE.pow(-x))
private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t