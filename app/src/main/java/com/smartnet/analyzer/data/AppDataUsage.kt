package com.smartnet.analyzer.data

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.painter.Painter
import com.smartnet.analyzer.R

data class AppDataUsage(
    val icon: Int = 0,
    val packageName: String = "",
    val usagePercentage: String = ""
)
