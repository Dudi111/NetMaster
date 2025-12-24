package com.smartnet.analyzer

import android.app.Application
import android.content.Context
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("dudi","My application started")
        mApplicationContext = applicationContext
    }

    companion object {
        var mApplicationContext: Context? = null
    }
}