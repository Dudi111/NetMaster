package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnet.analyzer.retrofit.RetrofitHelper
import com.smartnet.analyzer.utils.CoroutineHelper
import com.smartnet.analyzer.utils.IoDispatcher
import com.smartnet.analyzer.utils.IoScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import javax.inject.Inject
import kotlin.concurrent.timer

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    @IoScope val scope: CoroutineScope,
    @IoDispatcher val dispatcher: CoroutineDispatcher,
    private val retrofitHelper: RetrofitHelper
) : ViewModel() {

    private val _maxSpeed = MutableStateFlow("0")
    val maxSpeed: MutableStateFlow<String> = _maxSpeed

    private val _ping = MutableStateFlow("0")
    val ping: MutableStateFlow<String> = _ping

    private val _currentSpeed = MutableStateFlow("0")
    val currentSpeed: MutableStateFlow<String> = _currentSpeed

    private val _floatValue = MutableStateFlow(0f)
    val floatValue: MutableStateFlow<Float> = _floatValue

    var buttonState = mutableStateOf("START")

    fun onStartClick() {
        if (buttonState.value == "START") {
            buttonState.value = "connecting"
            viewModelScope.launch(dispatcher) {
                measureSpeedAndPing(
                    speedCallback = { currentSpeedValue ->
                        updateUIState(
                            currentSpeedValue,
                            0L,
                            maxSpeed,
                            currentSpeed,
                            ping,
                            floatValue
                        )
                    },
                    resultCallback = { speed, pingValue ->
                        updateUIState(
                            speed,
                            pingValue,
                            maxSpeed,
                            currentSpeed,
                            ping,
                            floatValue
                        )
                    }
                )
            }
        } else {
            buttonState.value = "START"
        }
    }

    private fun updateUIState(
        currentSpeedValue: Double,
        pingValue: Long,
        maxSpeed: MutableStateFlow<String>,
        currentSpeed: MutableStateFlow<String>,
        ping: MutableStateFlow<String>,
        floatValue: MutableStateFlow<Float>
    ) {
        Log.d("dudi","current speed value: $currentSpeedValue ,max speed: ${maxSpeed.value} , float value: ${floatValue.value}")
        if (currentSpeedValue > maxSpeed.value.toDoubleOrNull() ?: 0.0) {
            maxSpeed.value = String.format("%.1f", currentSpeedValue)
        }
        val formattedSpeed = String.format("%.1f", currentSpeedValue)
        currentSpeed.value = formattedSpeed
        ping.value = pingValue.toString()
        floatValue.value = (formattedSpeed.toDouble().toLong() / (maxSpeed.value.toDoubleOrNull() ?: 1.0)).toFloat()
    }

    private suspend fun measureSpeedAndPing(
        speedCallback: (Double) -> Unit,
        resultCallback: (Double, Long) -> Unit
    ) {

        val api = retrofitHelper.createSpeedApi()

        /* -------------------- PING -------------------- */

        val pingStart = System.nanoTime()

        val pingResponse = try {
            api.ping()
        } catch (e: Exception) {
            println("Ping failed: ${e.message}")
            resultCallback(-1.0, -1)
            return
        }

        if (!pingResponse.isSuccessful) {
            println("Ping error: ${pingResponse.code()}")
            resultCallback(-1.0, -1)
            return
        }

        val pingMs = (System.nanoTime() - pingStart) / 1_000_000
        println("Ping Successful: $pingMs ms")

        /* -------------------- DOWNLOAD -------------------- */

        val downloadStart = System.nanoTime()

        val downloadResponse = try {
            api.download(bytes = 100_000_000L) // ~100MB
        } catch (e: Exception) {
            println("Download failed: ${e.message}")
            resultCallback(-1.0, pingMs)
            return
        }

        if (!downloadResponse.isSuccessful) {
            println("Download error: ${downloadResponse.code()}")
            resultCallback(-1.0, pingMs)
            return
        }

        val body = downloadResponse.body() ?: run {
            println("Response body null")
            resultCallback(-1.0, pingMs)
            return
        }

        val inputStream = body.byteStream()
        val buffer = ByteArray(64 * 1024)

        var totalBytes = 0L
        var lastIntervalBytes = 0L

        val speedTimer = timer(period = 300) {
            val speedMBps =
                (lastIntervalBytes / (1024.0 * 1024.0)) * 2 // 500ms → per second
            speedCallback(speedMBps)
            lastIntervalBytes = 0L
        }

        buttonState.value = "STOP"
        try {
            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) break

                totalBytes += bytesRead
                lastIntervalBytes += bytesRead
            }
        } finally {
            speedTimer.cancel()
            inputStream.close()
        }

        val elapsedSeconds =
            (System.nanoTime() - downloadStart) / 1_000_000_000.0

        val finalSpeedMBps =
            (totalBytes / (1024.0 * 1024.0)) / elapsedSeconds

        Log.d("dudi","Download Completed: $finalSpeedMBps MB/s")
        resultCallback(finalSpeedMBps, pingMs)
    }
}