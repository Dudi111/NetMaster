package com.smartnet.analyzer.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RetrofitHelper @Inject constructor() {

    fun createSpeedApi(): CloudFlareSpeedApi {

        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Cache-Control", "no-store")
                    .header("Pragma", "no-cache")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://delicate-river-0fac.pd-rajiv-000.workers.dev/")
            .client(okHttpClient)
            .build()
            .create(CloudFlareSpeedApi::class.java)
    }
}