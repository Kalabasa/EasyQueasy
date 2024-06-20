package com.leanrada.easyqueasy.ui

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
val oneMeter = 1587f.dp
const val startEffectDurationMillis = 800L

enum class PreviewMode {
    NONE,
    SIZE,
    SPEED
}

@Composable
fun Overlay(
    appData: AppDataClient,
    peripherySize: Dp = 180.dp,
    previewMode: PreviewMode = PreviewMode.NONE,
) {
    val configuration = LocalConfiguration.current
    val isPreview = previewMode != PreviewMode.NONE

    val overlayAreaSize by appData.rememberOverlayAreaSize()
    val overlaySpeed by appData.rememberOverlaySpeed()
    val speedFactor by remember { derivedStateOf { lerp(0.2f, 2f, overlaySpeed) } }

    val startTimeMillis by remember {
        object : State<Long> {
            override val value: Long = System.currentTimeMillis()
        }
    }
    var currentTimeMillis by remember { mutableLongStateOf(startTimeMillis) }
    var timer by remember { mutableIntStateOf(0) }

    val position = remember { mutableStateListOf(0f, 0f, 0f) }
    var effectIntensity by remember { mutableFloatStateOf(0f) }

    val sensorManager = ContextCompat.getSystemService(LocalContext.current, SensorManager::class.java)

    DisposableEffect(sensorManager, isPreview, timer) {
        var accelerationListener: SensorEventListener? = null

        if (isPreview) {
            val now = System.currentTimeMillis()
            val dt = now - currentTimeMillis
            currentTimeMillis = now
            position[0] += 0.3f * speedFactor * dt
            timer++
        } else {
            accelerationListener = object : SensorEventListener {
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
                        currentTimeMillis = System.currentTimeMillis()
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
        }

        onDispose {
            if (accelerationListener != null) {
                sensorManager?.unregisterListener(accelerationListener)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        Log.d("", "currentTimeMillis: $currentTimeMillis")
        val finalEffectIntensity = when (previewMode) {
            PreviewMode.NONE -> effectIntensity
            else -> 1f
        }

        val startupEffectProgress = (currentTimeMillis - startTimeMillis).toFloat() / startEffectDurationMillis
        val startupEffectActive = !isPreview && startupEffectProgress in 0f..1f

        val baseDotRadius = 4.dp.toPx() * finalEffectIntensity

        if (baseDotRadius > 0.15f || startupEffectActive) {
            val scaledPeripherySizePx = when (previewMode) {
                PreviewMode.SPEED -> 30.dp.toPx()
                else -> peripherySize.toPx() *
                        lerp(0.2f, 1f, overlayAreaSize) *
                        lerp(0.4f, 1f, finalEffectIntensity).pow(2f)
            }

            val (offsetXPx, offsetYPx) = when (previewMode) {
                PreviewMode.NONE -> Pair(
                    position[0] * oneMeter.toPx() * speedFactor,
                    position[1] * oneMeter.toPx() * speedFactor
                )

                PreviewMode.SIZE -> Pair(0f, 0f)
                PreviewMode.SPEED -> Pair(position[0], 0f)
            }

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