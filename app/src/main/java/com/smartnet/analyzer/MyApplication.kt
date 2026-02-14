package com.smartnet.analyzer

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import ch.qos.logback.classic.LoggerContext
import com.dude.logfeast.logs.CustomLogUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.smartnet.analyzer.retrofit.DateChangedBroadcastReceiver
import dagger.hilt.android.HiltAndroidApp
import org.slf4j.LoggerFactory

@HiltAndroidApp
class MyApplication() : Application() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        Log.d("dudi","My application started")
        mApplicationContext = applicationContext

        //Logging Initialization
        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        lc.stop()

        FirebaseApp.initializeApp(this)
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        CustomLogUtils.logInit(
            context = this,
            storageRef = storageRef,
            packageName = "com.smartnet.analyzer",
            appName = "NetMaster",
            lc = lc,
            true
        )

        //Register DateChangeBroadCast receiver
        val dateChangedBroadcastReceiver = DateChangedBroadcastReceiver()
        applicationContext.registerReceiver(
            dateChangedBroadcastReceiver,
            DateChangedBroadcastReceiver.getIntentFilter(),
            RECEIVER_NOT_EXPORTED
        )
    }

    companion object {
        var mApplicationContext: Context? = null
    }
}