package com.smartnet.analyzer.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming

interface CloudFlareSpeedApi {

    @Streaming
    @GET("100MB.bin")
    suspend fun downloadFile(): Response<ResponseBody>

    // Ping endpoint (downloads only 1 byte)
    @GET("100MB.bin")
    suspend fun ping(@Header("Range") range: String = "bytes=0-0"): Response<ResponseBody>
}