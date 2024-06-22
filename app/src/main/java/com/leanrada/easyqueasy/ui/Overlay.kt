package com.leanrada.easyqueasy.ui

import AppDataOuterClass.OverlayColor
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils.clamp
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
val eyeZ = oneMeter.times(0.6f)
val screenDepth = oneMeter.times(0.2f)
const val startEffectDurationMillis = 700L

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

    val overlayColor by appData.rememberOverlayColor()
    val overlayAreaSize by appData.rememberOverlayAreaSize()
    val overlaySpeed by appData.rememberOverlaySpeed()
    val speedFactor by remember { derivedStateOf { lerp(0.6f, 2.7f, overlaySpeed) } }

    val startTimeMillis by remember {
        object : State<Long> {
            override val value: Long = System.currentTimeMillis()
        }
    }
    var currentTimeMillis by remember { mutableLongStateOf(startTimeMillis) }
    var timer by remember { mutableIntStateOf(0) }

    val position = remember { mutableStateListOf(0f, 0f, 0f) }
    val lastPosition = remember { mutableStateListOf(0f, 0f, 0f) }
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
                var lowPass: Array<Float>? = null
                var lowerPass: Array<Float>? = null
                var velocity = arrayOf(0f, 0f, 0f)

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    if (lastEventTimeNanos > 0) {
                        val dt = (event.timestamp - lastEventTimeNanos) * 1e-9f

                        var lowPass1 = lowPass
                        var lowerPass1 = lowerPass
                        if (lowPass1 != null && lowerPass1 != null) {
                            val tf = 1f - 0.01f.pow(dt)
                            lowPass1[0] += (event.values[0] - lowPass1[0]) * tf
                            lowPass1[1] += (event.values[1] - lowPass1[1]) * tf
                            lowPass1[2] += (event.values[2] - lowPass1[2]) * tf
                            val tf2 = 1f - 0.02f.pow(dt)
                            lowerPass1[0] += (event.values[0] - lowerPass1[0]) * tf2
                            lowerPass1[1] += (event.values[1] - lowerPass1[1]) * tf2
                            lowerPass1[2] += (event.values[2] - lowerPass1[2]) * tf2
                        } else {
                            lowPass1 = event.values.toTypedArray().clone()
                            lowerPass1 = event.values.toTypedArray().clone()
                            lowPass = lowPass1
                            lowerPass = lowerPass1
                        }

                        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                        val indexX = if (isPortrait) 0 else 1
                        val indexY = if (isPortrait) 1 else 0

                        val accelerationX = (event.values[indexX] - lowerPass1[indexX]) + (event.values[indexX] - lowPass1[indexX]) * 0.1f
                        val accelerationY = (event.values[indexY] - lowerPass1[indexY]) + (event.values[indexY] - lowPass1[indexY]) * 0.1f
                        val accelerationZ = (event.values[2] - lowerPass1[2]) + (event.values[2] - lowPass1[2]) * 0.1f

                        velocity[0] -= accelerationX * dt
                        velocity[1] += accelerationY * dt
                        velocity[2] += accelerationZ * dt

                        lastPosition[0] = position[0]
                        lastPosition[1] = position[1]
                        lastPosition[2] = position[2]

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
                        effectIntensity = lerp(effectIntensity, 1f, 1f - (1f - sigmoid01((speed2D - 0.09f) * 60f)).pow(72f * dt))
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
        val finalEffectIntensity = when (previewMode) {
            PreviewMode.NONE -> effectIntensity
            else -> 1f
        }

        val startupEffectProgress = (currentTimeMillis - startTimeMillis).toFloat() / startEffectDurationMillis
        val startupEffectActive = !isPreview && startupEffectProgress in 0f..1f

        val baseDotRadius = 4.dp.toPx() * finalEffectIntensity

        if (baseDotRadius > 0.15f || startupEffectActive) {
            val screenDepthPx = screenDepth.toPx()
            val scaledPeripherySizePx = when (previewMode) {
                PreviewMode.SPEED -> 30.dp.toPx()
                else -> peripherySize.toPx() *
                        lerp(0.2f, 1f, overlayAreaSize) *
                        lerp(0.4f, 1f, finalEffectIntensity).pow(2f)
            }

            val (offsetXPx, offsetYPx, offsetZPx) = when (previewMode) {
                PreviewMode.NONE -> Triple(
                    position[0] * oneMeter.toPx() * speedFactor,
                    position[1] * oneMeter.toPx() * speedFactor,
                    position[2] * oneMeter.toPx() * speedFactor,
                )

                PreviewMode.SIZE -> Triple(0f, 0f, 0f)
                PreviewMode.SPEED -> Triple(position[0], 0f, 0f)
            }

            val (trailDxPx, trailDyPx, trailDzPx) = when (previewMode) {
                PreviewMode.NONE -> Triple(
                    (position[0] - lastPosition[0]) * oneMeter.toPx(),
                    (position[1] - lastPosition[1]) * oneMeter.toPx(),
                    (position[2] - lastPosition[2]) * oneMeter.toPx(),
                )

                else -> Triple(0f, 0f, 0f)
            }

            val gridSizeX = 60f.dp.toPx()
            val gridSizeY = gridSizeX / hexRatio
            val gridSizeZ = screenDepthPx * 0.8f

            for (x in -2 until (size.width / gridSizeX + 2).toInt()) {
                for (y in -4 until (size.height / gridSizeY + 4).toInt()) {
                    for (z in -1 until (screenDepthPx / gridSizeZ + 2).toInt()) {
                        val pixelX = (x + 0.5f + (y % 2) * 0.5f) * gridSizeX + offsetXPx % gridSizeX
                        val pixelY = (y + 0.5f + (z % 2) * 1.3333f) * gridSizeY + offsetYPx % (gridSizeY * 2)
                        val pixelZ = z * gridSizeZ + offsetZPx % (gridSizeZ * 2)
                        val pixelTrailX = pixelX + trailDxPx
                        val pixelTrailZ = pixelZ + trailDzPx
                        if (overlayColor == OverlayColor.BLACK_AND_WHITE || overlayColor == OverlayColor.BLACK) {
                            val pixelTrailY = pixelY + trailDyPx
                            drawParticle(
                                pixelX,
                                pixelY,
                                pixelZ,
                                pixelTrailX,
                                pixelTrailY,
                                pixelTrailZ,
                                Color.Black,
                                baseDotRadius,
                                scaledPeripherySizePx,
                                startupEffectActive,
                                startupEffectProgress,
                            )
                        }
                        if (overlayColor == OverlayColor.BLACK_AND_WHITE || overlayColor == OverlayColor.WHITE) {
                            val whitePixelY = pixelY + gridSizeY * 0.6667f
                            val whitePixelTrailY = whitePixelY + trailDyPx
                            drawParticle(
                                pixelX,
                                whitePixelY,
                                pixelZ,
                                pixelTrailX,
                                whitePixelTrailY,
                                pixelTrailZ,
                                Color.White,
                                baseDotRadius - 1f,
                                scaledPeripherySizePx,
                                startupEffectActive,
                                startupEffectProgress,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawParticle(
    pixelX: Float,
    pixelY: Float,
    pixelZ: Float,
    pixelTrailX: Float,
    pixelTrailY: Float,
    pixelTrailZ: Float,
    color: Color,
    baseDotRadius: Float,
    scaledPeripherySizePx: Float,
    startupEffectActive: Boolean,
    startupEffectProgress: Float,
) {
    val eyeZPx = eyeZ.toPx()
    val screenDepthPx = screenDepth.toPx()
    val dotRadius = 2f * dotRadius(
        pixelX,
        pixelY,
        pixelZ,
        baseDotRadius,
        size,
        screenDepthPx,
        scaledPeripherySizePx,
        startupEffectActive,
        startupEffectProgress
    )
    if (dotRadius > 0f) {
        val start = perspective(pixelX, pixelY, pixelZ, size)
        val end = perspective(pixelTrailX, pixelTrailY, pixelTrailZ, size)
        drawLine(
            color = color,
            alpha = 1f - min(abs((pixelZ - screenDepthPx * 0.5f) / (screenDepthPx * 0.5f)), 1f).pow(2),
            cap = StrokeCap.Round,
            start = start,
            end = end,
            strokeWidth = max(1f, dotRadius - end.minus(start).getDistance() * 0.44f) * (eyeZPx / (pixelZ + eyeZPx))
        )
    }
}

private fun Density.perspective(x: Float, y: Float, z: Float, screenSize: Size): Offset {
    val eyeZPx = eyeZ.toPx()
    return Offset(
        screenSize.width * 0.5f + (x - screenSize.width * 0.5f) * (eyeZPx / (z + eyeZPx)),
        screenSize.height * 0.5f + (y - screenSize.height * 0.5f) * (eyeZPx / (z + eyeZPx)),
    )
}

private fun Density.dotRadius(
    x: Float,
    y: Float,
    z: Float,
    baseDotRadius: Float,
    screenSize: Size,
    screenDepthPx: Float,
    scaledPeripherySizePx: Float,
    startupEffectActive: Boolean,
    startupEffectProgress: Float
) = (
        max(
            0f,
            baseDotRadius * dotRadiusFactor(x, y, z, screenSize, screenDepthPx, scaledPeripherySizePx)
        ) + startUpEffectRadius(startupEffectActive, startupEffectProgress, y, screenSize))

private fun Density.dotRadiusFactor(x: Float, y: Float, z: Float, screenSize: Size, screenDepthPx: Float, peripherySize: Float): Float {
    return clamp(
        1f - min(1f, edgeDistance(x, y, z, screenSize) / peripherySize),
        0f,
        1f
    )
}

private fun Density.edgeDistance(x: Float, y: Float, z: Float, screenSize: Size): Float {
    val (px, py) = perspective(x, y, z, screenSize)
    return max(0f, min(min(px, py), min(screenSize.width - px, screenSize.height - py)))
}

private fun startUpEffectRadius(startupEffectActive: Boolean, startupEffectProgress: Float, y: Float, screenSize: Size): Float =
    if (startupEffectActive) {
        val span = lerp(0.1f, 0.7f, startupEffectProgress)
        12f * max(
            0f,
            (span - abs((1f - y / screenSize.height) - lerp(-0.1f, 1.7f, startupEffectProgress.pow(2f)))) / span
        ).pow(2)
    } else
        0f

private fun accelerationCurve(x: Float) = x * sigmoid01((abs(x) - 2e-12f) * 1e4f)
private fun sigmoid01(x: Float) = 1f / (1f + floatE.pow(-x))
private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t