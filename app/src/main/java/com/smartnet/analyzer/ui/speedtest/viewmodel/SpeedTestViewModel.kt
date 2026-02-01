package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnet.analyzer.data.UIState
import com.smartnet.analyzer.retrofit.RetrofitHelper
import com.smartnet.analyzer.utils.IoDispatcher
import com.smartnet.analyzer.utils.IoScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    @IoScope val scope: CoroutineScope,
    @IoDispatcher val dispatcher: CoroutineDispatcher,
    private val retrofitHelper: RetrofitHelper
) : ViewModel() {

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