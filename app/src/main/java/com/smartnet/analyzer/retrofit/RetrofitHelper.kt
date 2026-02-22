package com.smartnet.analyzer.retrofit

import com.smartnet.analyzer.utils.Constants.BASE_URL
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
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(CloudFlareSpeedApi::class.java)
    }
}