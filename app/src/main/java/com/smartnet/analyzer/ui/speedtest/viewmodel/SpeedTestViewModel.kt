package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.R
import com.smartnet.analyzer.data.UIState
import com.smartnet.analyzer.retrofit.RetrofitHelper
import com.smartnet.analyzer.utils.GlobalFunctions
import com.smartnet.analyzer.utils.IoDispatcher
import com.smartnet.analyzer.utils.IoScope
import com.smartnet.analyzer.utils.SpeedTestConstants.INTERNET_ERROR
import com.smartnet.analyzer.utils.SpeedTestConstants.SPEED_TEST_ERROR
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
    var speedTestJob: Job? = null

    var currentInputStream: InputStream? = null

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
            LogFeast.info("Start button clicked")
            peakSpeed = 0f
            _uiState.value = UIState(btnState = "connecting")
            measureSpeedAndPing()
        } else if (btnText == "STOP") {
            LogFeast.info("Stop button clicked")

            scope.launch(dispatcher) {
                try {
                    currentInputStream?.close()
                } catch (e: Exception) {
                    LogFeast.error("Exception in closing input stream:", e)
                } finally {
                    speedTestJob?.cancel()
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
            val pingMs = runCatching {
                val pingStart = System.nanoTime()

                val pingResponse = api.ping()

                // Always close body
                pingResponse.body()?.close()

                if (!pingResponse.isSuccessful) {
                    LogFeast.warn("ping failed: ${pingResponse.code()}")
                    return@launch
                }

                ((System.nanoTime() - pingStart) / 1_000_000).toInt()
            }.getOrNull()

            if (pingMs != null) onPingMeasured(pingMs)

            /* ---- DOWNLOAD ---- */
            val response = api.downloadFile()

            val body = response.body()
            if (body == null) {
                LogFeast.debug("Download body is null")
                showErrorDialog()
                return@launch
            }
            currentInputStream = body.byteStream()
            val buffer = ByteArray(64 * 1024)

            _uiState.update { it.copy(btnState = "STOP") }
            var lastBytes = 0L

            val speedJob = launch {
                while (isActive) {
                    delay(500)
                    LogFeast.debug("Last byte: $lastBytes")
                    val speed =
                        (lastBytes / (1024f * 1024f)) * (1000f / 300f)
                    lastBytes = 0
                    LogFeast.debug("speed: $speed")
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
                LogFeast.error("Exception in download:", e)
            } finally {
                speedJob.cancel()
                LogFeast.debug("Download finished")
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

    /**
     * showErrorDialog: This method is used to show the error dialog
     */
    private fun showErrorDialog() {
        dialogID = SPEED_TEST_ERROR
        dialogMessage = R.string.speed_test_error
        dialogState.value = true
    }
}