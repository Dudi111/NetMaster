package com.smartnet.analyzer.data

import android.graphics.drawable.Drawable

data class AppDataUsage(
    val icon: Drawable? ,
    val uid: Int = 0,
    val appName: String = "",
    var rxBytes: Long,
    var txBytes: Long
) {
    val totalBytes: Long
        get() = (rxBytes + txBytes)
}