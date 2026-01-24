package com.smartnet.analyzer.ui.charts.viewmodel

import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.data.DataUsageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    private val dataUsageHelper: DataUsageHelper,
): ViewModel() {

    var dailyDataUsage: List<Float> = getDailyDataUsageBytes().map { bytesToMb(it) }


    fun bytesToMb(bytes: Long): Float {
        return bytes / (1024f * 1024f)
    }

    fun getDailyDataUsageBytes(): MutableList<Long> {
        val dailyDataUsage = mutableListOf<Long>()
        val range = getDailyTimeRanges()
        range.forEach { it ->
            val simUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_CELLULAR)

            val wifiUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_WIFI)

            Log.d("dudi", "range 1st : ${it.first} , range 2nd: ${it.second}")
            Log.d("dudi", "sim usage: $simUsage , wifi usage: $wifiUsage")
            dailyDataUsage.add(simUsage + wifiUsage)
        }
            return dailyDataUsage
    }

    fun getDailyTimeRanges(): List<Pair<Long, Long>> {
        val ranges = mutableListOf<Pair<Long, Long>>()
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentMonth = cal.get(Calendar.MONTH)

        while (cal.get(Calendar.MONTH) == currentMonth) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = cal.timeInMillis
            ranges.add(dayStart to dayEnd)
        }

        return ranges
    }

}