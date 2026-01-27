package com.smartnet.analyzer.ui.charts.viewmodel

import android.content.Context
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
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
import com.smartnet.analyzer.utils.GlobalFunctions.bytesToMb
import com.smartnet.analyzer.utils.GlobalFunctions.formatBytes
import com.smartnet.analyzer.utils.GlobalFunctions.getTimeRange
import com.smartnet.analyzer.utils.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    @ApplicationContext val context: Context,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    private val dataUsageHelper: DataUsageHelper,
) : ViewModel() {

    //state to show dialog
    var dialogState = mutableStateOf(false)

    //state to update monthly usage
    var overallUsageDetail = mutableStateOf(MonthlyUsage("", ""))
    //state to update network wise usage
    var networkUsageDetail = mutableStateOf(MonthlyUsage("", ""))
    //state holding the selected app
    var selectedApp = mutableStateOf(
        Triple(
            ContextCompat.getDrawable(context, R.drawable.ic_default_app)!!,
            second = "Select app",
            third = 0
        )
    )
    // selected app index
    var selectedAppIndex = mutableIntStateOf(-1)
    //list of all the apps
    var userAppList: List<AppDataUsage>? = null
    //state showing the total usage of selected app
    var appWiseTotalUsage = mutableStateOf("0")

    var thisMonthTotalUsage = 0L
    var lastMonthTotalUsage = 0L

    val thisMonthModelProducer = CartesianChartModelProducer()
    val networkWiseModelProducer = CartesianChartModelProducer()
    val appWiseModelProducer = CartesianChartModelProducer()
    val lastMonthModelProducer = CartesianChartModelProducer()


    init {
        loadThisMonthOverallUsage()
        loadNetworkUsage(NETWORK_TYPE_CELLULAR)
        loadLastMonthOverallUsage()
        loadAppList()
    }


    /**
     * loadThisMonthOverallUsage: This method will fetch and load this month data usage to model producer
     */
    fun loadThisMonthOverallUsage() {
        viewModelScope.launch(ioDispatcher) {
            val data = getDailyDataUsageBytes(getDailyRangesForMonth())
            thisMonthModelProducer.runTransaction {
                lineSeries {
                    series(data)
                }
            }
        }
    }

    /**
     * loadLastMonthOverallUsage: This method will fetch and load last month data usage to model producer
     */
    fun loadLastMonthOverallUsage() {
        viewModelScope.launch(ioDispatcher) {
            val data = getDailyDataUsageBytes(getDailyRangesForMonth(-1), false)
            lastMonthModelProducer.runTransaction {
                lineSeries {
                    series(data)
                }
            }
        }
    }

    /**
     * loadNetworkUsage: This method will fetch and load this month network specific data usage to model producer
     * @param networkType: WIFI or CELLULAR
     */
    fun loadNetworkUsage(
        networkType: String,
    ) {
        var total = 0L
        val range = getDailyRangesForMonth()
        val usage = mutableListOf<Long>()
        viewModelScope.launch(ioDispatcher) {
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
            val data = usage.map { bytesToMb(it) }
            networkWiseModelProducer.runTransaction {
                columnSeries {
                    series(data)
                }
            }
        }
    }

    /**
     * loadAppWiseUsage: This method will fetch and load this month app specific data usage to model producer
     */
    fun loadAppWiseUsage(
        uid: Int,
    ) {
        var total = 0L
        val dailyDataUsage = mutableListOf<Long>()
        val range = getDailyRangesForMonth()
        viewModelScope.launch(ioDispatcher) {
            Log.d("dudi", "getting app wise data usage: $uid")
            range.forEach {
                Log.d("dudi", "range start: ${it.first} , end range: ${it.second}")
                val simUsage = dataUsageHelper.getUidDataUsage(
                    NetworkCapabilities.TRANSPORT_CELLULAR,
                    uid,
                    it.first,
                    it.second
                )
                val wifiUsage = dataUsageHelper.getUidDataUsage(
                    NetworkCapabilities.TRANSPORT_WIFI,
                    uid,
                    it.first,
                    it.second
                )

                dailyDataUsage.add(simUsage + wifiUsage)
                total += (simUsage + wifiUsage)
            }
            appWiseTotalUsage.value = formatBytes(total)
            val data = dailyDataUsage.map { bytesToMb(it) }
            appWiseModelProducer.runTransaction {
                columnSeries {
                    series(data)
                }
            }
        }
    }

    /**
     * loadAppList: This method will fetch and load all the apps to userAppList
     */
    fun loadAppList() {
        viewModelScope.launch(ioDispatcher) {
            val (startTime, endTime) = getTimeRange(DATA_USAGE_TODAY)
            //get app in descending order of usage
            userAppList = dataUsageHelper.getAppDataUsage(
                startTime,
                endTime,
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
        }
    }

    /**
     * onConfirmClick: This method will be called when user confirms the app selection
     * @param index: index of the selected app
     */
    fun onConfirmClick(index: Int) {
        selectedApp.value =
            Triple(
                userAppList!![index].icon!!,
                userAppList!![index].appName,
                userAppList!![index].uid
            )
        selectedAppIndex.intValue = index
        loadAppWiseUsage(userAppList!![selectedAppIndex.intValue].uid)
        dialogState.value = false
    }

    /**
     * getDailyDataUsageBytes: This method will fetch the daily data usage in bytes for the given range
     * @param range: start time and end time of day and list of days
     */
    fun getDailyDataUsageBytes(range: List<Pair<Long, Long>>, isCurrentMonth: Boolean = true): List<Float> {
        var total = 0L
        val dailyDataUsage = mutableListOf<Long>()
        range.forEach {
            val simUsage = dataUsageHelper.getDayWiseDataUsage(
                it.first,
                it.second,
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
            val wifiUsage = dataUsageHelper.getDayWiseDataUsage(
                it.first,
                it.second,
                NetworkCapabilities.TRANSPORT_WIFI
            )

            dailyDataUsage.add(simUsage + wifiUsage)
            total += simUsage + wifiUsage
        }
        if (isCurrentMonth) thisMonthTotalUsage = total else lastMonthTotalUsage = total

        getMonthYearFromMillis(range.first().first, total, overallUsageDetail)
        return dailyDataUsage.map { bytesToMb(it) }
    }

    /**
     * getMonthYearFromMillis: This method will fetch the month and year from the given time in millis and update state to show on UI
     * @param timeInMillis: time in millis
     */
    fun getMonthYearFromMillis(timeInMillis: Long, total: Long, state: MutableState<MonthlyUsage>) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val month = Instant.ofEpochMilli(timeInMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        val usage = formatBytes(total)
        state.value = MonthlyUsage(month, usage)
    }

    /**
     * getDailyRangesForMonth: This method will return the list of day start & end time in millis for whole month
     * @param monthOffset: 0 = current month, -1 = last month & 1 = next month
     */
    fun getDailyRangesForMonth(
        monthOffset: Long = 0, // 0 = current month, -1 = last month
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Pair<Long, Long>> {

        val now = ZonedDateTime.now(zoneId)
        val todayStart = now.toLocalDate().atStartOfDay(zoneId)

        val monthStart = now
            .plusMonths(monthOffset)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(zoneId)

        val monthEnd = monthStart
            .plusMonths(1)
            .minusNanos(1)

        val effectiveMonthEnd =
            if (monthOffset.toInt() == 0) todayStart.plusDays(1).minusNanos(1)
            else monthEnd

        val ranges = mutableListOf<Pair<Long, Long>>()
        var currentDayStart = monthStart

        while (!currentDayStart.isAfter(effectiveMonthEnd)) {

            val nextDayStart = currentDayStart.plusDays(1)

            // Stop creating future days for current month
            if (monthOffset.toInt() == 0 && currentDayStart.isAfter(todayStart)) {
                break
            }

            val dayStartMillis = currentDayStart.toInstant().toEpochMilli()
            val dayEndMillis = minOf(
                nextDayStart.minusNanos(1).toInstant().toEpochMilli(),
                now.toInstant().toEpochMilli()
            )

            ranges.add(dayStartMillis to dayEndMillis)
            currentDayStart = nextDayStart
        }

        return ranges
    }

    /**
     * getCurrentMonthShortName: This method will return the short name of current month
     * @param millis: time in millis
     */
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