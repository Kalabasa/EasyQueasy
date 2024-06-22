package com.leanrada.easyqueasy

import AppDataOuterClass.AppData
import AppDataOuterClass.DrawingMode
import AppDataOuterClass.OverlayColor
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class AppDataClient(context: Context, scope: CoroutineScope) {
    val dataStore: DataStore<AppData> = MultiProcessDataStoreFactory.create(
        serializer = AppDataSerializer(),
        produceFile = {
            File("${context.cacheDir.path}/app_data.pb")
        },
        scope = scope
    )

    @Composable
    fun rememberLoaded(): State<Boolean> = dataStore.data.map { true }.collectAsState(initial = false)

    @Composable
    fun rememberOnboarded(): MutableState<Boolean> = rememberAppData(
        dataStore,
        { data -> data.hasOnboarded() },
        { data -> data.onboarded },
        { data, value -> data.onboarded = value },
    )

    @Composable
    fun rememberOnboardedAccessibilitySettings(): MutableState<Boolean> = rememberAppData(
        dataStore,
        { data -> data.hasOnboardedAccessibilitySettings() },
        { data -> data.onboardedAccessibilitySettings },
        { data, value -> data.onboardedAccessibilitySettings = value },
    )

    @Composable
    fun rememberDrawingMode(): MutableState<DrawingMode> = rememberAppData(
        dataStore,
        { data -> data.hasDrawingMode() },
        { data -> data.drawingMode },
        { data, value -> data.drawingMode = value },
    )

    @Composable
    fun rememberOverlayColor(): MutableState<OverlayColor> = rememberAppData(
        dataStore,
        { data -> data.hasOverlayColor() },
        { data -> data.overlayColor },
        { data, value -> data.overlayColor = value },
    )

    @Composable
    fun rememberOverlayAreaSize(): MutableState<Float> = rememberAppData(
        dataStore,
        { data -> data.hasOverlayAreaSize() },
        { data -> data.overlayAreaSize },
        { data, value -> data.overlayAreaSize = value },
        0.5f,
    )

    @Composable
    fun rememberOverlaySpeed(): MutableState<Float> = rememberAppData(
        dataStore,
        { data -> data.hasOverlaySpeed() },
        { data -> data.overlaySpeed },
        { data, value -> data.overlaySpeed = value },
        0.5f,
    )

    @Composable
    fun rememberForegroundOverlayStartTime(): MutableState<Long> = rememberAppData(
        dataStore,
        { data -> data.hasForegroundOverlayStartTime() },
        { data -> data.foregroundOverlayStartTime },
        { data, value -> data.foregroundOverlayStartTime = value },
        0L,
    )

    @Composable
    fun rememberForegroundOverlayStopTime(): MutableState<Long> = rememberAppData(
        dataStore,
        { data -> data.hasForegroundOverlayStopTime() },
        { data -> data.foregroundOverlayStopTime },
        { data, value -> data.foregroundOverlayStopTime = value },
        0L,
    )
}

class AppDataSerializer : Serializer<AppData> {
    override val defaultValue: AppData = AppData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppData =
        try {
            AppData.parseFrom(input.readBytes())
        } catch (serialization: InvalidProtocolBufferException) {
            throw CorruptionException("Unable to read AppData", serialization)
        }

    override suspend fun writeTo(t: AppData, output: OutputStream) {
        t.writeTo(output)
    }
}

@Composable
private fun <T> rememberAppData(
    dataStore: DataStore<AppData>,
    has: (AppData) -> Boolean,
    get: (AppData) -> T,
    set: (AppData.Builder, value: T) -> Unit,
    initial: T = get(AppData.getDefaultInstance())
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()

    val state = dataStore.data
        .map { get(it) }
        .collectAsState(initial)

    return object : MutableState<T> {
        override var value: T
            get() = state.value
            set(value) {
                coroutineScope.launch {
                    try {
                        dataStore.updateData {
                            val builder = it.toBuilder()
                            set(builder, value)
                            builder.build()
                        }
                    } catch (e: Exception) {
                        Log.e(AppDataClient::class.simpleName, "Update data failed!", e)
                    }
                }
            }

        override fun component1() = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}
