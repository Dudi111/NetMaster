package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.R
import com.smartnet.analyzer.data.UIState
import com.smartnet.analyzer.retrofit.RetrofitHelper
import com.smartnet.analyzer.utils.GlobalFunctions
import com.smartnet.analyzer.utils.IoDispatcher
import com.smartnet.analyzer.utils.IoScope
import com.smartnet.analyzer.utils.SpeedTestConstants.INTERNET_ERROR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    @param:IoScope val scope: CoroutineScope,
    @param:IoDispatcher val dispatcher: CoroutineDispatcher,
    private val retrofitHelper: RetrofitHelper
) : ViewModel() {

    var dialogState = mutableStateOf(false)

    var dialogID = R.string.empty

    var dialogMessage = 0

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState

    private var peakSpeed = 0f
    private var speedTestJob: Job? = null

    private var currentInputStream: InputStream? = null

    /**
     * onStartClick: This method is used to start the speed test
     */
    @SuppressLint("SuspiciousIndentation")
    fun onStartClick(btnText: String) {
        if (btnText == "START") {
            if (!GlobalFunctions.isInternetAvailable()) {
                dialogID = INTERNET_ERROR
                dialogMessage = R.string.internet_error
                dialogState.value = true
                return
            }
            Log.d("dudi", "start button clicked")
            peakSpeed = 0f
            _uiState.value = UIState(btnState = "connecting")
            measureSpeedAndPing()
        } else if (btnText == "STOP") {
            Log.d("dudi", "Stop button clicked")
//            scope.cancel()
//            Log.d("dudi", "is job null: ${speedTestJob == null}")
//            speedTestJob?.cancel()
            scope.launch(dispatcher) {   // dispatcher = Dispatchers.IO
                try {
                    currentInputStream?.close()
                } catch (e: Exception) {
                    Log.e("dudi", "Error closing stream: $e")
                } finally {
                    currentInputStream = null
                }
            }
            _uiState.update {
                it.copy(
                    btnState = "START",
                    currentSpeedMbps = 0f,
                    speedometerProgress = 0f
                )
            }
        }
    }

    /**
     * onSpeedUpdate: This method is used to update the speed
     */
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

    /**
     * onPingMeasured: This method is used to measure the ping
     */
    fun onPingMeasured(pingMs: Int) {
        _uiState.update { it.copy(pingMs = pingMs) }
    }

    /**
     * measureSpeedAndPing: This method is used to measure the speed and ping
     */
    private fun measureSpeedAndPing() {
        speedTestJob?.cancel()
        speedTestJob = scope.launch(dispatcher) {

            val api = retrofitHelper.createSpeedApi()

            /* ---- PING ---- */
//            val pingStart = System.nanoTime()
//            val pingResponse = runCatching { api.ping() }.getOrNull() //?: return@launch
//            if (pingResponse == null) {
//                Log.d("dudi", "ping failed")
//                return@launch
//            }
//            if (!pingResponse.isSuccessful) return@launch
//
//            val pingMs = ((System.nanoTime() - pingStart) / 1_000_000).toInt()
//            onPingMeasured(pingMs)

            /* ---- DOWNLOAD ---- */
            val response = api.downloadFile()
            Log.d("dudi", "download response: $response")
            Log.d("dudi", "Is response successful: ${response.isSuccessful}")
            Log.d("dudi", "Response message is: ${response.message()}")
            Log.d("dudi", "Response code is: ${response.errorBody()?.string()}")

            val body = response.body() //?: return@launch
            if (body == null) {
                Log.d("dudi", "download body is null")
                return@launch
            }
            currentInputStream = body.byteStream()
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
                    val read = currentInputStream!!.read(buffer)
                    if (read == -1) break
                    lastBytes += read
                }

            } catch (e: Exception) {
                speedJob.cancel()
                Log.e("dudi", "error in last loop: $e")
            } finally {
                speedJob.cancel()
                Log.e("dudi", "download completed finally")
                currentInputStream?.close()
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