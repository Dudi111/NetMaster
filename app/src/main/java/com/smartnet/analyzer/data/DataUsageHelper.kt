package com.smartnet.analyzer.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataUsageHelper @Inject constructor(
    @ApplicationContext context: Context
) {


    fun getTodayAppWiseMobileDataUsage(context: Context): List<AppDataUsage> {

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val packageManager = context.packageManager
        val (startTime, endTime) = 0L, 0L
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
}