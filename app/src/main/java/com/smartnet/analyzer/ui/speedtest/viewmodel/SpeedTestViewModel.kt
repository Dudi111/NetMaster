package com.smartnet.analyzer.ui.speedtest.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
class SpeedTestViewModel @Inject constructor() : ViewModel() {

    private val _maxSpeed = MutableStateFlow("0")
    val maxSpeed: MutableStateFlow<String> = _maxSpeed

    private val _ping = MutableStateFlow("0")
    val ping: MutableStateFlow<String> = _ping

    private val _currentSpeed = MutableStateFlow("0")
    val currentSpeed: MutableStateFlow<String> = _currentSpeed

    private val _floatValue = MutableStateFlow(0f)
    val floatValue: MutableStateFlow<Float> = _floatValue

    fun onStartClick() {
        viewModelScope.launch {
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
            maxSpeed.value = currentSpeedValue.toString()
        }
        val formattedSpeed = String.format("%.1f", currentSpeedValue)
        currentSpeed.value = formattedSpeed
        ping.value = pingValue.toString()
        floatValue.value = (formattedSpeed.toDouble().toLong() / (maxSpeed.value.toDoubleOrNull() ?: 1.0)).toFloat()
    }

    fun measureSpeedAndPing(speedCallback: (Double) -> Unit, resultCallback: (Double, Long) -> Unit) {
        val url = "https://nbg1-speed.hetzner.com/100MB.bin"
        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1)) // ✅ Force HTTP/1.1
            .retryOnConnectionFailure(true) // ✅ Enable retries
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        // Measure Ping using a small GET request (first 1 KB)
        val pingStart = System.nanoTime()
        val pingRequest = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Range", "bytes=0-1023") // Only 1 KB for ping measurement
            .build()

        client.newCall(pingRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("⚠️ Ping Request Failed: ${e.message}")
                resultCallback(-1.0, -1)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { // Automatically closes response body
                    val pingEnd = System.nanoTime()
                    val pingMs = (pingEnd - pingStart) / 1_000_000 // Convert to ms
                    println("Ping Successful: $pingMs ms")

                    // Start Full Download
                    val startTime = System.nanoTime()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            println("Download Request Failed: ${e.message}")
                            resultCallback(-1.0, pingMs)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use { //Automatically closes response body
                                if (!response.isSuccessful) {
                                    println("Server Error: ${response.code}")
                                    resultCallback(-1.0, pingMs)
                                    return
                                }

                                response.body?.let { responseBody ->
                                    val inputStream = responseBody.byteStream()
                                    val buffer = ByteArray(65536) // Increased buffer size (64KB)
                                    var bytesRead: Int
                                    var totalBytes = 0L
                                    val fileSize = 100 * 1024 * 1024L // 100MB in bytes

                                    var lastSecondBytes = 0L
                                    val speedTimer = timer(period = 500) { // Every 1 second
                                        val speedMBps = (lastSecondBytes / (1024.0 * 1024.0)) // Convert bytes to MB
                                        speedCallback(speedMBps)
                                        lastSecondBytes = 0L // Reset counter for next second
                                    }

                                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                        totalBytes += bytesRead
                                        lastSecondBytes += bytesRead
                                    }

                                    // Stop timer
                                    speedTimer.cancel()

                                    // Calculate Final Speed
                                    val endTime = System.nanoTime()
                                    val elapsedTime = (endTime - startTime) / 1_000_000_000.0 // Convert nanoseconds to seconds
                                    val finalSpeedMBps = (totalBytes / (1024.0 * 1024.0)) / elapsedTime // MB per second

                                    println("Download Completed: $finalSpeedMBps MB/s")
                                    resultCallback(finalSpeedMBps, pingMs)
                                } ?: run {
                                    println("Response Body is Null")
                                    resultCallback(-1.0, pingMs)
                                }
                            } //Closes response automatically
                        }
                    })
                }
            }
        })
    }
}