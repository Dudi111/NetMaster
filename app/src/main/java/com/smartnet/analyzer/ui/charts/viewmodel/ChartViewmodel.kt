package com.smartnet.analyzer.ui.charts.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnet.analyzer.R
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.data.DataUsageHelper
import com.smartnet.analyzer.utils.Constants.DATA_USAGE_TODAY
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_CELLULAR
import com.smartnet.analyzer.utils.GlobalFunctions.getTimeRange
import com.smartnet.analyzer.utils.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    @ApplicationContext val context: Context,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    private val dataUsageHelper: DataUsageHelper,
): ViewModel() {

    var dialogState = mutableStateOf(false)
    var overallUsageDetail = mutableStateOf(Pair("", ""))
    var networkUsageDetail = mutableStateOf(Pair("", ""))
    var selectedApp = mutableStateOf(Triple(ContextCompat.getDrawable(context, R.drawable.ic_default_app)!!, second = "Select app", third = 0))
    var selectedAppIndex = mutableStateOf(-1)
    var userAppList: List<AppDataUsage>? = null
    var appWiseTotalUsage = mutableStateOf("0")
    var networkUsage = mutableStateOf("0")

    private val _thisMonthOverallDataUsage = MutableStateFlow<List<Float>>(emptyList())
    val thisMonthOverallDatalUsage: StateFlow<List<Float>> = _thisMonthOverallDataUsage

    private val _lastMonthOverallDataUsage = MutableStateFlow<List<Float>>(emptyList())
    val lastMonthOverallDatalUsage: StateFlow<List<Float>> = _lastMonthOverallDataUsage

    private val _networkDataUsage = MutableStateFlow<List<Float>>(emptyList())
    val networkDataUsage: StateFlow<List<Float>> = _networkDataUsage


    private val _appWiseDataUsage = MutableStateFlow<List<Float>>(emptyList())
    val appWiseDataUsage: StateFlow<List<Float>> = _appWiseDataUsage

    init {
        loadThisMonthOverallUsage()
        loadNetworkUsage(NETWORK_TYPE_CELLULAR)
        loadLastMonthOverallUsage()
        loadAppList()
    }

    fun loadThisMonthOverallUsage() {
        viewModelScope.launch(ioDispatcher) {
                val data = getDailyDataUsageBytes(getDailyTimeRanges())
            _thisMonthOverallDataUsage.emit(data)
        }
    }

    fun loadLastMonthOverallUsage() {
        viewModelScope.launch(ioDispatcher) {
            val data =getDailyDataUsageBytes(getLastMonthStartEndMillis())
            _lastMonthOverallDataUsage.emit(data)
        }
    }

    fun loadNetworkUsage(
        networkType: String,
    ) {
        viewModelScope.launch(ioDispatcher) {
            _networkDataUsage.emit(getNetworkType(networkType))
        }
    }

    fun loadAppWiseUsage(
        uid: Int,
    ) {
        viewModelScope.launch(ioDispatcher) {
            _appWiseDataUsage.emit(getAppDataUsage(uid))
        }
    }

    fun bytesToMb(bytes: Long): Float {
        return bytes / (1024f * 1024f)
    }

    fun loadAppList() {
        viewModelScope.launch(ioDispatcher) {
            val (startTime, endTime) = getTimeRange(DATA_USAGE_TODAY)
            userAppList = dataUsageHelper.getAppDataUsage(startTime, endTime, NetworkCapabilities.TRANSPORT_CELLULAR)
        }
    }

    fun onConfirmClick(index: Int) {
        selectedApp.value =
            Triple(userAppList!![index].icon!!, userAppList!![index].appName, userAppList!![index].uid)
        selectedAppIndex.value = index
        loadAppWiseUsage(userAppList!![selectedAppIndex.value].uid)
        dialogState.value = false
    }

    fun getDailyDataUsageBytes(range: List<Pair<Long, Long>>): List<Float> {
        var total = 0L
        val dailyDataUsage = mutableListOf<Long>()
        range.forEach {
            val simUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_CELLULAR)
            val wifiUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_WIFI)

            dailyDataUsage.add(simUsage + wifiUsage)
            total += (simUsage + wifiUsage)
        }
            getMonthYearFromMillis(range.first().first, total)
            return dailyDataUsage.map { bytesToMb(it) }
    }

    fun getMonthYearFromMillis(timeInMillis: Long, total: Long) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val month =  Instant.ofEpochMilli(timeInMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        val usage = formatBytes(total)
        overallUsageDetail.value = Pair(month, usage)
    }

    fun getNetworkType(networkType: String): List<Float> {
        var total = 0L
        return if (networkType == NETWORK_TYPE_CELLULAR) {
            val cellUsage = mutableListOf<Long>()
            val range = getDailyTimeRanges()
            range.forEach {
                val simUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_CELLULAR)
                total += simUsage
                cellUsage.add(simUsage)
            }
            networkUsage.value = formatBytes(total)
            cellUsage.map { bytesToMb(it) }
        } else {
            val wifiUsage = mutableListOf<Long>()
            val range = getDailyTimeRanges()
            range.forEach {
                val usage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_WIFI)
                total += usage
                wifiUsage.add(usage)
            }
            networkUsage.value = formatBytes(total)
            wifiUsage.map { bytesToMb(it) }
        }
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

        // Today at start of day
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        while (cal.timeInMillis <= todayStart) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = minOf(cal.timeInMillis, System.currentTimeMillis())
            ranges.add(dayStart to dayEnd)
        }

        return ranges
    }


    fun getAppDataUsage(uid: Int): List<Float> {
        Log.d("dudi","getting app wise data usage: $uid")
        var total = 0L
        val dailyDataUsage = mutableListOf<Long>()
        val range = getDailyTimeRanges()
        range.forEach {
            val simUsage = dataUsageHelper.getUidDataUsage( NetworkCapabilities.TRANSPORT_CELLULAR, uid, it.first, it.second)
            val wifiUsage = dataUsageHelper.getUidDataUsage( NetworkCapabilities.TRANSPORT_WIFI, uid, it.first, it.second)

            dailyDataUsage.add(simUsage + wifiUsage)
            total += (simUsage + wifiUsage)
        }
        appWiseTotalUsage.value = formatBytes(total)
        return dailyDataUsage.map { bytesToMb(it) }
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

    fun getLastMonthStartEndMillis(): List<Pair<Long, Long>> {
        val zoneId = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zoneId)

        // Start of last month
        val startOfLastMonth = now
            .minusMonths(1)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(zoneId)

        // End of last month
        val endOfLastMonth = startOfLastMonth
            .plusMonths(1)
            .minusNanos(1)

        val ranges = mutableListOf<Pair<Long, Long>>()

        var currentDayStart = startOfLastMonth

        while (!currentDayStart.isAfter(endOfLastMonth)) {
            val nextDayStart = currentDayStart.plusDays(1)

            val dayStartMillis = currentDayStart.toInstant().toEpochMilli()
            val dayEndMillis = nextDayStart
                .minusNanos(1)
                .toInstant()
                .toEpochMilli()

            ranges.add(dayStartMillis to dayEndMillis)

            currentDayStart = nextDayStart
        }

        return ranges
    }
}