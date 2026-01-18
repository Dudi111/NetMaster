package com.smartnet.analyzer.ui.datausage.viewmodel

import android.content.Context
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.data.DataUsageHelper
import com.smartnet.analyzer.utils.Constants
import com.smartnet.analyzer.utils.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject


@HiltViewModel
class DataUsageViewmodel @Inject constructor(
    @ApplicationContext context: Context,
    private val dataUsageHelper: DataUsageHelper,
    @IoDispatcher var ioDispatcher: CoroutineDispatcher,
) : ViewModel(){

    var progressState = mutableStateOf(false)
    var uiState = mutableStateOf(false)
    var dataList = mutableListOf(AppDataUsage(icon = null, txBytes = 0L, rxBytes = 0L))

    val dateRanges = listOf(
        Constants.DATA_USAGE_THIS_MONTH,
        Constants.DATA_USAGE_THIS_WEEK,
        Constants.DATA_USAGE_TODAY,
        Constants.DATA_USAGE_YESTERDAY
    )
    val networkType = listOf(Constants.NETWORK_TYPE_CELLULAR, Constants.NETWORK_TYPE_WIFI)

    var totalUsage by mutableStateOf("")
        private set

    fun updateTotalUsage() {
        val totalBytes = dataList.sumOf { it.totalBytes }
        totalUsage = formatBytes(totalBytes)
    }


    fun getDataUsage() {
        val (startTime, endTime) = getTimeRange(dateRanges[2])
        viewModelScope.launch(ioDispatcher) {
            dataList = dataUsageHelper.getAppDataUsage(startTime, endTime, NetworkCapabilities.TRANSPORT_CELLULAR).toMutableList()
            updateTotalUsage()
            progressState.value = false
            uiState.value= true
        }
    }

    fun onDateNetworkChange(dateRange: String, network: String) {
        uiState.value = false
        progressState.value = true
        val (startTime, endTime) = getTimeRange(dateRange)
        viewModelScope.launch(ioDispatcher) {
            dataList = dataUsageHelper.getAppDataUsage(startTime, endTime, getNetworkType(network)).toMutableList()
            updateTotalUsage()
            progressState.value = false
            uiState.value= true
        }
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


    fun getTimeRange(
        type: String,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {

        val now = LocalDate.now(zoneId)

        val (startDateTime, endDateTime) = when (type) {

            Constants.DATA_USAGE_TODAY -> {
                val start = now.atStartOfDay(zoneId)
                val end = now.plusDays(1).atStartOfDay(zoneId).minusNanos(1)
                start to end
            }

            Constants.DATA_USAGE_YESTERDAY -> {
                val yesterday = now.minusDays(1)
                val start = yesterday.atStartOfDay(zoneId)
                val end = now.atStartOfDay(zoneId).minusNanos(1)
                start to end
            }

            Constants.DATA_USAGE_THIS_WEEK -> {
                val startOfWeek = now.with(DayOfWeek.MONDAY)
                val start = startOfWeek.atStartOfDay(zoneId)
                val end = start.plusWeeks(1).minusNanos(1)
                start to end
            }

            Constants.DATA_USAGE_THIS_MONTH -> {
                val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                val start = startOfMonth.atStartOfDay(zoneId)
                val end = start.plusMonths(1).minusNanos(1)
                start to end
            }

            else -> {
                val start = now.atStartOfDay(zoneId)
                val end = now.plusDays(1).atStartOfDay(zoneId).minusNanos(1)
                start to end
            }
        }

        return Pair(
             startDateTime.toInstant().toEpochMilli(),
             endDateTime.toInstant().toEpochMilli()
        )
    }

    fun getNetworkType(type: String): Int {
        return when(type) {
            Constants.NETWORK_TYPE_CELLULAR -> NetworkCapabilities.TRANSPORT_CELLULAR
            Constants.NETWORK_TYPE_WIFI -> NetworkCapabilities.TRANSPORT_WIFI
            else -> NetworkCapabilities.TRANSPORT_CELLULAR
        }
    }
}