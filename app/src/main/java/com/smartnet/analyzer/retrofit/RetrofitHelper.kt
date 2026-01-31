package com.smartnet.analyzer.retrofit

import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RetrofitHelper @Inject constructor() {

    fun createSpeedApi(): CloudFlareSpeedApi {

        val okHttpClient = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .retryOnConnectionFailure(true)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://speed.cloudflare.com/")
            .client(okHttpClient)
            .build()
            .create(CloudFlareSpeedApi::class.java)
    }
}