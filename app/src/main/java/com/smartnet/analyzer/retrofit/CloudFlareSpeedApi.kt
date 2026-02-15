package com.smartnet.analyzer.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface CloudFlareSpeedApi {

    @Streaming
    @GET("100MB.bin")
    suspend fun downloadFile(): Response<ResponseBody>

    @GET("ping")
    suspend fun ping(): Response<ResponseBody>
}