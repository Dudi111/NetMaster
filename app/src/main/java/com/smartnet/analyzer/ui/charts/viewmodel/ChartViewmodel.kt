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
import com.smartnet.analyzer.R
import com.smartnet.analyzer.data.DataUsageHelper
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_CELLULAR
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    @ApplicationContext val context: Context,
    private val dataUsageHelper: DataUsageHelper,
): ViewModel() {

    var dialogState = mutableStateOf(false)
    var dailyDataUsage: List<Float> = getDailyDataUsageBytes().map { bytesToMb(it) }

    var selectedApp = mutableStateOf(Triple(ContextCompat.getDrawable(context, R.drawable.ic_default_app)!!, second = "Select app", third = 0))

    var selectedAppIndex = mutableStateOf(-1)
    var userAppList = getUserInstalledApps(context)

    var appWiseTotalUsage = mutableStateOf("0")

    var overallUsageDetail = mutableStateOf(Pair("", ""))

    fun bytesToMb(bytes: Long): Float {
        return bytes / (1024f * 1024f)
    }

    fun updateNetworkUsage(networkType: String): List<Float> {
        return getNetworkType(networkType).map { bytesToMb(it) }
    }

    fun getDailyDataUsageBytes(): MutableList<Long> {
        val dailyDataUsage = mutableListOf<Long>()
        val range = getDailyTimeRanges()
        range.forEach {
            val simUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_CELLULAR)

            val wifiUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_WIFI)

            Log.d("dudi", "range 1st : ${it.first} , range 2nd: ${it.second}")
            Log.d("dudi", "sim usage: $simUsage , wifi usage: $wifiUsage")
            dailyDataUsage.add(simUsage + wifiUsage)
        }
            return dailyDataUsage
    }

    fun getNetworkType(networkType: String): List<Long> {
        return if (networkType == NETWORK_TYPE_CELLULAR) {
            val cellUsage = mutableListOf<Long>()
            val range = getDailyTimeRanges()
            range.forEach {
                val simUsage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_CELLULAR)
                cellUsage.add(simUsage)
            }
            cellUsage
        } else {
            val wifiUsage = mutableListOf<Long>()
            val range = getDailyTimeRanges()
            range.forEach {
                val usage = dataUsageHelper.getDayWiseDataUsage(it.first, it.second, NetworkCapabilities.TRANSPORT_WIFI)
                wifiUsage.add(usage)
            }
            wifiUsage
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

    fun getUserInstalledApps(context: Context): List<Triple<Drawable, String, Int>> {
        val pm = context.packageManager
        val apps = mutableListOf<Triple<Drawable, String, Int>>()
        apps.add(Triple(ContextCompat.getDrawable(context, R.drawable.ic_default_app)!!, second = "Select app", third = 0))

        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (appInfo in installedApps) {

            // ❌ Exclude system apps
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue

            // Optional: Exclude updated system apps
            if ((appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) continue

            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            val uid = appInfo.uid

            apps.add(
                Triple(
                    first = icon,
                    second = appName,
                    third = uid,
                )
            )
        }

        return apps.sortedBy { it.second.lowercase() }
    }

    fun getAppDataUsage(uid: Int): List<Float> {
        var total = 0L
        val dailyDataUsage = mutableListOf<Long>()
        val range = getDailyTimeRanges()
        range.forEach {
            val simUsage = dataUsageHelper.getUidDataUsage( NetworkCapabilities.TRANSPORT_CELLULAR, uid, it.first, it.second)

            val wifiUsage = dataUsageHelper.getUidDataUsage( NetworkCapabilities.TRANSPORT_WIFI, uid, it.first, it.second)

            Log.d("dudi", "range 1st : ${it.first} , range 2nd: ${it.second}")
            Log.d("dudi", "sim usage: $simUsage , wifi usage: $wifiUsage")
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
}