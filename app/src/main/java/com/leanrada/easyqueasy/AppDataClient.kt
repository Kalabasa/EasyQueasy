package com.leanrada.easyqueasy

import AppDataOuterClass.AppData
import AppDataOuterClass.DrawingMode
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class AppDataClient(context: Context) {
    private val dataStore: DataStore<AppData> = MultiProcessDataStoreFactory.create(
        serializer = AppDataSerializer(),
        produceFile = {
            File("${context.cacheDir.path}/app_data.pb")
        }
    )

    @Composable
    fun rememberOnboarded(): MutableState<Boolean> = rememberAppData(
        dataStore,
        { it.onboarded },
        { it, value -> it.onboarded = value },
    )

    @Composable
    fun rememberOnboardedAccessibilitySettings(): MutableState<Boolean> = rememberAppData(
        dataStore,
        { it.onboardedAccessibilitySettings },
        { it, value -> it.onboardedAccessibilitySettings = value },
    )

    @Composable
    fun rememberDrawingMode(): MutableState<DrawingMode> = rememberAppData(
        dataStore,
        { it.drawingMode },
        { it, value -> it.drawingMode = value },
    )

    @Composable
    fun rememberOverlayAreaSize(): MutableState<Float> = rememberAppData(
        dataStore,
        { it.overlayAreaSize },
        { it, value -> it.overlayAreaSize = value },
    )

    @Composable
    fun rememberOverlaySpeed(): MutableState<Float> = rememberAppData(
        dataStore,
        { it.overlaySpeed },
        { it, value -> it.overlaySpeed = value },
    )
}

class AppDataSerializer : Serializer<AppData> {
    override val defaultValue = AppData.getDefaultInstance()

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
    get: (AppData) -> T,
    set: (AppData.Builder, value: T) -> Unit
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()

    val currentState = remember { mutableStateOf(get(AppData.getDefaultInstance())) }

    dataStore.data
        .map { get(it) }
        .onEach { currentState.value = it }
        .collectAsState(initial = currentState.value)

    return object : MutableState<T> {
        override var value: T
            get() = currentState.value
            set(value) {
                val rollbackValue = currentState.value
                currentState.value = value
                coroutineScope.launch {
                    try {
                        dataStore.updateData {
                            val builder = it.toBuilder()
                            set(builder, value)
                            builder.build()
                        }
                    } catch (e: Exception) {
                        Log.e("EQ", e.toString())
                        currentState.value = rollbackValue
                    }
                }
            }

        override fun component1() = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}
