package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    @IoDispatcher val dispatcher: CoroutineDispatcher
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
    val normalScope = CoroutineHelper.getNormalScope(dispatcher)

    fun onStartClick() {
        if (buttonState.value == "START") {
            buttonState.value = "connecting"
            normalScope.launch {
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
            normalScope.cancel()
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
        Log.d("dudi","current speed value: $currentSpeedValue ,max speed: ${maxSpeed.value} , float value: $floatValue")
        if (currentSpeedValue > maxSpeed.value.toDoubleOrNull() ?: 0.0) {
            maxSpeed.value = String.format("%.1f", currentSpeedValue)
        }
        val formattedSpeed = String.format("%.1f", currentSpeedValue)
        currentSpeed.value = formattedSpeed
        ping.value = pingValue.toString()
        floatValue.value = (formattedSpeed.toDouble().toLong() / (maxSpeed.value.toDoubleOrNull() ?: 1.0)).toFloat()
    }


    fun measureSpeedAndPing(
        speedCallback: (Double) -> Unit,
        resultCallback: (Double, Long) -> Unit
    ) {

        // Cloudflare endpoints
        val pingUrl = "https://speed.cloudflare.com/__down?bytes=0"
        val downloadUrl = "https://speed.cloudflare.com/__down?bytes=100000000" // ~100MB

        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1)) // Keep HTTP/1.1
            .retryOnConnectionFailure(true)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        /* -------------------- PING -------------------- */

        val pingStart = System.nanoTime()
        val pingRequest = Request.Builder()
            .url(pingUrl)
            .addHeader("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(pingRequest).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                println("Ping Request Failed: ${e.message}")
                resultCallback(-1.0, -1)
            }

            override fun onResponse(call: Call, response: Response) {
                response.close()

                val pingEnd = System.nanoTime()
                val pingMs = (pingEnd - pingStart) / 1_000_000
                println("Ping Successful: $pingMs ms")

                /* -------------------- DOWNLOAD -------------------- */

                val startTime = System.nanoTime()
                val downloadRequest = Request.Builder()
                    .url(downloadUrl)
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build()

                client.newCall(downloadRequest).enqueue(object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        println("Download Request Failed: ${e.message}")
                        resultCallback(-1.0, pingMs)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {

                            if (!response.isSuccessful) {
                                println("Server Error: ${response.code}")
                                resultCallback(-1.0, pingMs)
                                return
                            }

                            val body = response.body ?: run {
                                println("Response Body is Null")
                                resultCallback(-1.0, pingMs)
                                return
                            }

                            buttonState.value = "STOP"
                            val inputStream = body.byteStream()
                            val buffer = ByteArray(64 * 1024) // 64KB buffer

                            var bytesRead: Int
                            var totalBytes = 0L
                            var lastIntervalBytes = 0L

                            val speedTimer = timer(period = 500) {
                                val speedMBps =
                                    (lastIntervalBytes / (1024.0 * 1024.0)) * 2 // 500ms → per second
                                speedCallback(speedMBps)
                                lastIntervalBytes = 0L
                            }

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                totalBytes += bytesRead
                                lastIntervalBytes += bytesRead
                            }

                            speedTimer.cancel()

                            val endTime = System.nanoTime()
                            val elapsedSeconds =
                                (endTime - startTime) / 1_000_000_000.0

                            val finalSpeedMBps =
                                (totalBytes / (1024.0 * 1024.0)) / elapsedSeconds

                            println("Download Completed: $finalSpeedMBps MB/s")
                            resultCallback(finalSpeedMBps, pingMs)
                        }
                    }
                })
            }
        })
    }

}