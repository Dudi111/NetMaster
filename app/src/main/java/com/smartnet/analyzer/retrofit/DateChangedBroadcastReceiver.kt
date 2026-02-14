package com.smartnet.analyzer.retrofit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.dude.logfeast.logs.CustomLogUtils
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class DateChangedBroadcastReceiver : BroadcastReceiver() {

    /**
     * onReceive : Method would execute when date change broadcast received
     * @param context use for access the application-specific resources and classes
     * @param intent use for getting the which action performed
     */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_DATE_CHANGED) {
            LogFeast.debug("Date change broadcast received, uploading log file")
            CustomLogUtils.uploadLogFile(context)
        }
    }

    companion object {
        /**
         * getIntentFilter : Create the [IntentFilter] for the [DateChangedBroadcastReceiver].
         * @return The [IntentFilter]
         */
        fun getIntentFilter() = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
        }
    }
}

