package com.smartnet.analyzer.ui.datausage.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.data.DataUsageHelper
import com.smartnet.analyzer.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class DataUsageViewmodel @Inject constructor(
    @ApplicationContext context: Context,
    private val dataUsageHelper: DataUsageHelper
) : ViewModel(){

    var dataList = mutableListOf(AppDataUsage(icon = null, txBytes = 0L, rxBytes = 0L))

    val dateRanges = listOf(
        Constants.DATA_USAGE_THIS_MONTH,
        Constants.DATA_USAGE_THIS_WEEK,
        Constants.DATA_USAGE_TODAY,
        Constants.DATA_USAGE_YESTERDAY
    )
    val networkType = listOf(Constants.NETWORK_TYPE_CELLULAR, Constants.NETWORK_TYPE_WIFI)

    init {
        val (startTime, endTime) = getTodayStartEndMillis()
        dataList = dataUsageHelper.getAppDataUsage(startTime, endTime).toMutableList()
    }

    fun getTodayStartEndMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis

        cal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val end = cal.timeInMillis

        return start to end
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            bytes >= gb -> String.format("%.2f GB", bytes / gb)
            bytes >= mb -> String.format("%.2f MB", bytes / mb)
            bytes >= kb -> String.format("%.2f KB", bytes / kb)
            else -> "$bytes B"
        }
    }
}