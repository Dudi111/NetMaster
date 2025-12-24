package com.smartnet.analyzer.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.smartnet.analyzer.R


data class AppDataUsage(
    val icon: Drawable? ,
    val packageName: String = "",
    val appName: String = "",
    val rxBytes: Long,
    val txBytes: Long
) {
    val totalBytes: Long
        get() = ((rxBytes + txBytes)/1024)
}
