package com.smartnet.analyzer.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Streaming

interface CloudFlareSpeedApi {

    @GET("__down")
    suspend fun ping(
        @Query("bytes") bytes: Int = 0,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0"
    ): Response<ResponseBody>

    @Streaming
    @GET("__down")
    suspend fun download(
        @Query("bytes") bytes: Long,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0"
    ): Response<ResponseBody>

}