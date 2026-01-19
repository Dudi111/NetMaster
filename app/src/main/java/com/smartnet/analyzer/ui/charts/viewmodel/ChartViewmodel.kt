package com.smartnet.analyzer.ui.charts.viewmodel

import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.data.DataUsageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    private val dataUsageHelper: DataUsageHelper,
): ViewModel() {



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