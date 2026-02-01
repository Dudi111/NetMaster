package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.data.UIState
import com.smartnet.analyzer.retrofit.RetrofitHelper
import com.smartnet.analyzer.utils.IoDispatcher
import com.smartnet.analyzer.utils.IoScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    @param:IoScope val scope: CoroutineScope,
    @param:IoDispatcher val dispatcher: CoroutineDispatcher,
    private val retrofitHelper: RetrofitHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    private var peakSpeed = 0f
    private var speedTestJob: Job? = null

    @SuppressLint("SuspiciousIndentation")
    fun onStartClick(btnText: String) {
        if (btnText == "START") {
            Log.d("dudi", "start button clicked")
            peakSpeed = 0f
            _uiState.value = UIState(btnState = "connecting")
            measureSpeedAndPing()
        } else if (btnText == "STOP") {
            Log.d("dudi", "Stop button clicked")
            speedTestJob?.cancel()
            _uiState.update {
                it.copy(
                    btnState = "START",
                    currentSpeedMbps = 0f,
                    speedometerProgress = 0f
                )
            }
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

    private fun measureSpeedAndPing() {
        speedTestJob?.cancel()
        speedTestJob = scope.launch(dispatcher) {

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

            _uiState.update { it.copy(btnState = "STOP") }
            var lastBytes = 0L

            val speedJob = launch {
                while (isActive) {
                    delay(300)
                    Log.d("dudi", "last byte: $lastBytes")
                    val speed =
                        (lastBytes / (1024f * 1024f)) * (1000f / 300f)
                    lastBytes = 0
                    Log.d("dudi", "speed: $speed")
                    onSpeedUpdate(speed)
                }
            }

            try {
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    lastBytes += read
                }
                speedJob.cancel()
            } catch (e: Exception) {
                Log.e("dudi", "error in last loop: $e")
            } finally {
                speedJob.cancel()
                Log.e("dudi", "download completed finally")
                input.close()
                _uiState.update {
                    it.copy(
                        btnState = "START",
                        currentSpeedMbps = 0f,
                        speedometerProgress = 0f
                    )
                }
            }
        }
    }
}