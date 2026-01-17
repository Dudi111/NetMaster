package com.smartnet.analyzer.ui.datausage.viewmodel

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.data.DataUsageHelper
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

    init {
        val (startTime, endTime) = getTodayStartEndMillis()
        dataList = dataUsageHelper.getAppDataUsage(startTime, endTime).toMutableList()
       // dataList = getTodayAppWiseMobileDataUsage(context).toMutableList()
    }

    fun getTodayAppWiseMobileDataUsage(context: Context): List<AppDataUsage> {

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val packageManager = context.packageManager
        val (startTime, endTime) = getTodayStartEndMillis()

        val usageMap = mutableMapOf<Int, Pair<Long, Long>>() // uid → rx, tx

        val stats = networkStatsManager.querySummary(
            ConnectivityManager.TYPE_MOBILE,
            null,
            startTime,
            endTime
        )

        val bucket = NetworkStats.Bucket()
        while (stats.hasNextBucket()) {
            stats.getNextBucket(bucket)
            val uid = bucket.uid
            Log.d("dudi","App uid: $uid")
            val prev = usageMap[uid] ?: (0L to 0L)
            usageMap[uid] = Pair(
                prev.first + bucket.rxBytes,
                prev.second + bucket.txBytes
            )
        }
        stats.close()

        val appList = mutableListOf<AppDataUsage>()

        usageMap.forEach { (uid, bytes) ->
            try {
                val packages = packageManager.getPackagesForUid(uid) ?: return@forEach
                for (pkg in packages) {
                    val appInfo = packageManager.getApplicationInfo(pkg, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)
                    Log.d("dudi","app info: $appInfo , name: $appName , and icon: $icon")

                    appList.add(
                        AppDataUsage(
                            packageName = pkg,
                            appName = appName,
                            icon = icon,
                            rxBytes = bytes.first,
                            txBytes = bytes.second
                        )
                    )
                }
            } catch (e: Exception) {
                Log.d("dudi","Error while getting names: $e")
            }
        }

        return appList.sortedByDescending { it.totalBytes }
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

}