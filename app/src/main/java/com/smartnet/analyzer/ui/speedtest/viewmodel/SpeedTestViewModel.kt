package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnet.analyzer.data.UIState
import com.smartnet.analyzer.retrofit.RetrofitHelper
import com.smartnet.analyzer.utils.CoroutineHelper
import com.smartnet.analyzer.utils.IoDispatcher
import com.smartnet.analyzer.utils.IoScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    private var peakSpeed = 0f

    @SuppressLint("SuspiciousIndentation")
    fun onStartClick() {
        if (buttonState.value == "START") {
            buttonState.value = "connecting"
            peakSpeed = 0f
            _uiState.value = UIState(isRunning = true)
            startSpeedTest()
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

//    private fun measureSpeedAndPing() = scope.launch {
//
//        val api = retrofitHelper.createSpeedApi()
//
//        /* -------------------- PING -------------------- */
//
//        val pingStart = System.nanoTime()
//
//        val pingResponse = try {
//            api.ping()
//        } catch (e: Exception) {
//            Log.e("dudi", "Ping failed", e)
//            _ping.value = "0"
//            return@launch
//        }
//
//        if (!pingResponse.isSuccessful) {
//            _ping.value = "0"
//            return@launch
//        }
//
//        val pingMs = (System.nanoTime() - pingStart) / 1_000_000
//        _ping.value = pingMs.toString()
//
//        /* -------------------- DOWNLOAD -------------------- */
//
//        val downloadStart = System.nanoTime()
//
//        val downloadResponse = try {
//            api.download(bytes = 100_000_000L)
//        } catch (e: Exception) {
//            Log.e("dudi", "Download failed", e)
//            return@launch
//        }
//
//        if (!downloadResponse.isSuccessful) return@launch
//
//        val body = downloadResponse.body() ?: return@launch
//
//        val inputStream = body.byteStream()
//        val buffer = ByteArray(64 * 1024)
//
//        var totalBytes = 0L
//        var lastIntervalBytes = 0L
//
//        buttonState.value = "STOP"
//
//        /* -------------------- LIVE SPEED JOB -------------------- */
//
//        val speedJob = launch {
//            while (isActive) {
//                delay(300)
//
//                val speedMBps =
//                    (lastIntervalBytes / (1024.0 * 1024.0)) * (1000.0 / 300.0)
//
//                _currentSpeed.value = "%.2f".format(speedMBps)
//                _floatValue.value = speedMBps.toFloat()
//
//                lastIntervalBytes = 0L
//            }
//        }
//
//        try {
//            while (true) {
//                val bytesRead = inputStream.read(buffer)
//                if (bytesRead == -1) break
//
//                totalBytes += bytesRead
//                lastIntervalBytes += bytesRead
//            }
//        } finally {
//            speedJob.cancel()
//            inputStream.close()
//        }
//
//        /* -------------------- FINAL SPEED -------------------- */
//
//        val elapsedSeconds =
//            (System.nanoTime() - downloadStart) / 1_000_000_000.0
//
//        val finalSpeedMBps =
//            (totalBytes / (1024.0 * 1024.0)) / elapsedSeconds
//
//        _maxSpeed.value = "%.2f".format(finalSpeedMBps)
//    }

    fun onSpeedUpdate(speedMbps: Float) {
        peakSpeed = maxOf(peakSpeed, speedMbps)

        val progress = (speedMbps / 25f).coerceIn(0f, 1f)

        _uiState.update {
            it.copy(
                currentSpeedMbps = speedMbps,
                maxSpeedMbps = peakSpeed,
                speedometerProgress = progress
            )
        }
    }

    fun onPingMeasured(pingMs: Int) {
        _uiState.update { it.copy(pingMs = pingMs) }
    }

    private fun startSpeedTest() {
        viewModelScope.launch(dispatcher) {
            measureSpeedAndPing()
        }
    }

    private fun measureSpeedAndPing() = scope.launch{
        val api = retrofitHelper.createSpeedApi()

        /* ---- PING ---- */
        val pingStart = System.nanoTime()
        val pingResponse = runCatching { api.ping() }.getOrNull() ?: return@launch
        if (!pingResponse.isSuccessful) return@launch

        val pingMs = ((System.nanoTime() - pingStart) / 1_000_000).toInt()
        onPingMeasured(pingMs)

        /* ---- DOWNLOAD ---- */
        val response = api.download(100_000_000L)
        val body = response.body() ?: return@launch
        val input = body.byteStream()
        val buffer = ByteArray(64 * 1024)

        var lastBytes = 0L

        val speedJob = launch {
            while (isActive) {
                delay(300)
                val speed =
                    (lastBytes / (1024f * 1024f)) * (1000f / 300f)
                lastBytes = 0
                onSpeedUpdate(speed)
            }
        }

        try {
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                lastBytes += read
            }
        } finally {
            speedJob.cancel()
            input.close()
            _uiState.update { it.copy(isRunning = false) }
        }
    }
}