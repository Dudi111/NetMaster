package com.smartnet.analyzer.ui

import ComposeSpeedTestTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.smartnet.analyzer.data.UIState
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import kotlin.concurrent.timer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ComposeSpeedTestTheme {
                val coroutineScope = rememberCoroutineScope()
                val animation = remember { Animatable(0f) }
                val maxSpeed = remember { mutableStateOf("0") }
                val ping = remember { mutableStateOf("0") }
                val currentSpeed = remember { mutableStateOf("0") }
                val floatValue = remember { mutableStateOf(0f) }



                Surface {
                    SpeedTestScreenMain(
                        animation = animation,
                        maxSpeed = maxSpeed,
                        ping = ping,
                        currentSpeed = currentSpeed,
                        floatValue = floatValue
                    ) {
                        coroutineScope.launch {
                            measureSpeedAndPing(
                                speedCallback = { currentSpeedValue ->
                                    updateUIState(currentSpeedValue, 0L, maxSpeed, currentSpeed, ping, floatValue)
                                },
                                resultCallback = { speed, pingValue ->
                                    updateUIState(speed, pingValue, maxSpeed, currentSpeed, ping, floatValue)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateUIState(
        currentSpeedValue: Double,
        pingValue: Long,
        maxSpeed: MutableState<String>,
        currentSpeed: MutableState<String>,
        ping: MutableState<String>,
        floatValue: MutableState<Float>
    ) {

        if (currentSpeedValue > maxSpeed.value.toDoubleOrNull() ?: 0.0) {
            maxSpeed.value = currentSpeedValue.toString()
        }
        val formattedSpeed = String.format("%.1f", currentSpeedValue)
        currentSpeed.value = formattedSpeed
        ping.value = pingValue.toString()
        floatValue.value = (formattedSpeed.toLong() / (maxSpeed.value.toDoubleOrNull() ?: 1.0)).toFloat()
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
            .addHeader("Range", "bytes=0-1023") // ✅ Only 1 KB for ping measurement
            .build()

        client.newCall(pingRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("⚠️ Ping Request Failed: ${e.message}")
                resultCallback(-1.0, -1)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { // ✅ Automatically closes response body
                    val pingEnd = System.nanoTime()
                    val pingMs = (pingEnd - pingStart) / 1_000_000 // Convert to ms
                    println("✅ Ping Successful: $pingMs ms")

                    // Start Full Download
                    val startTime = System.nanoTime()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            println("⚠️ Download Request Failed: ${e.message}")
                            resultCallback(-1.0, pingMs)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use { // ✅ Automatically closes response body
                                if (!response.isSuccessful) {
                                    println("⚠️ Server Error: ${response.code}")
                                    resultCallback(-1.0, pingMs)
                                    return
                                }

                                response.body?.let { responseBody ->
                                    val inputStream = responseBody.byteStream()
                                    val buffer = ByteArray(65536) // ✅ Increased buffer size (64KB)
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

                                    println("✅ Download Completed: $finalSpeedMBps MB/s")
                                    resultCallback(finalSpeedMBps, pingMs)
                                } ?: run {
                                    println("⚠️ Response Body is Null")
                                    resultCallback(-1.0, pingMs)
                                }
                            } // ✅ Closes response automatically
                        }
                    })
                }
            }
        })
    }






}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
fun DefaultPreview() {
    ComposeSpeedTestTheme {
        Surface() {
            SpeedTestView(
                UIState(
                    speed = "11.5",
                    ping = "5 ms",
                    maxSpeed = "150.0 mbps",
                    arcValue = 0.1f,
                )
            ) { }
        }
    }
}