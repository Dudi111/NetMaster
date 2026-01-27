package com.smartnet.analyzer.ui.charts.viewmodel

import android.content.Context
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.smartnet.analyzer.R
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.data.DataUsageHelper
import com.smartnet.analyzer.data.MonthlyUsage
import com.smartnet.analyzer.utils.Constants.DATA_USAGE_TODAY
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_CELLULAR
import com.smartnet.analyzer.utils.GlobalFunctions.formatBytes
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
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    @ApplicationContext val context: Context,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    private val dataUsageHelper: DataUsageHelper,
): ViewModel() {

    var dialogState = mutableStateOf(false)
    var overallUsageDetail = mutableStateOf(MonthlyUsage("",""))
    var networkUsageDetail = mutableStateOf(MonthlyUsage("",""))
    var selectedApp = mutableStateOf(Triple(ContextCompat.getDrawable(context, R.drawable.ic_default_app)!!, second = "Select app", third = 0))
    var selectedAppIndex = mutableStateOf(-1)
    var userAppList: List<AppDataUsage>? = null
    var appWiseTotalUsage = mutableStateOf("0")

    private val _networkDataUsage = MutableStateFlow<List<Float>>(emptyList())
    val networkDataUsage: StateFlow<List<Float>> = _networkDataUsage


    private val _appWiseDataUsage = MutableStateFlow<List<Float>>(emptyList())
    val appWiseDataUsage: StateFlow<List<Float>> = _appWiseDataUsage

    val modelProducer = CartesianChartModelProducer()
    val modelProducer2 = CartesianChartModelProducer()
    val modelProducer3 = CartesianChartModelProducer()

    init {
        loadThisMonthOverallUsage()
        loadNetworkUsage(NETWORK_TYPE_CELLULAR)
        loadLastMonthOverallUsage()
        loadAppList()
    }

    fun loadThisMonthOverallUsage() {
        viewModelScope.launch(ioDispatcher) {
                val data = getDailyDataUsageBytes(getDailyTimeRanges())
            modelProducer.runTransaction {
                lineSeries {
                    series(data)
                }
            }
        }
    }

    fun loadLastMonthOverallUsage() {
        viewModelScope.launch(ioDispatcher) {
            val data =getDailyDataUsageBytes(getLastMonthStartEndMillis())
            modelProducer.runTransaction {
                lineSeries {
                    series(data)
                }
            }
        }
    }

    fun loadNetworkUsage(
        networkType: String,
    ) {
        viewModelScope.launch(ioDispatcher) {
            val data = getNetworkType(networkType)
            modelProducer2.runTransaction {
                columnSeries {
                    series(data)
                }
            }
        }
    }

    fun loadAppWiseUsage(
        uid: Int,
    ) {
        viewModelScope.launch(ioDispatcher) {
            val data = getAppDataUsage(uid)
            modelProducer3.runTransaction {
                columnSeries {
                    series(data)
                }
            }
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
            getMonthYearFromMillis(range.first().first, total, overallUsageDetail)
            return dailyDataUsage.map { bytesToMb(it) }
    }

    fun getMonthYearFromMillis(timeInMillis: Long, total: Long, state: MutableState<MonthlyUsage>) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val month =  Instant.ofEpochMilli(timeInMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        val usage = formatBytes(total)
        state.value = MonthlyUsage(month, usage)
    }

    fun getNetworkType(networkType: String): List<Float> {
        var total = 0L
        val range = getDailyTimeRanges()
        val usage = mutableListOf<Long>()
            range.forEach {
                    val networkUsage = dataUsageHelper.getDayWiseDataUsage(
                        startTime = it.first,
                        endTime = it.second,
                        networkType = if (networkType == NETWORK_TYPE_CELLULAR) NetworkCapabilities.TRANSPORT_CELLULAR else NetworkCapabilities.TRANSPORT_WIFI
                    )
                    total += networkUsage
                usage.add(networkUsage)
            }
        getMonthYearFromMillis(range.first().first, total, networkUsageDetail)
        return usage.map { bytesToMb(it) }
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
            Log.d("dudi","range start: ${it.first} , end range: ${it.second}")
            val simUsage = dataUsageHelper.getUidDataUsage( NetworkCapabilities.TRANSPORT_CELLULAR, uid, it.first, it.second)
            val wifiUsage = dataUsageHelper.getUidDataUsage( NetworkCapabilities.TRANSPORT_WIFI, uid, it.first, it.second)

            dailyDataUsage.add(simUsage + wifiUsage)
            total += (simUsage + wifiUsage)
        }
        appWiseTotalUsage.value = formatBytes(total)
        return dailyDataUsage.map { bytesToMb(it) }
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

    fun getCurrentMonthShortName(
        millis: Long = System.currentTimeMillis()
    ): String {
        val locale: Locale = Locale.getDefault()
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .month
            .getDisplayName(TextStyle.SHORT, locale)
    }
}