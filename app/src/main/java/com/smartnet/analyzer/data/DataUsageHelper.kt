package com.smartnet.analyzer.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.RemoteException
import android.util.Log
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.use

class DataUsageHelper @Inject constructor(
    @ApplicationContext context: Context
) {


    fun getTodayAppWiseMobileDataUsage(context: Context): List<AppDataUsage> {

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val packageManager = context.packageManager
        val (startTime, endTime) =
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

    private fun getAppDataUsage(
        startTime: Long,
        endTime: Long
    ): Map<String, Long> {
        val networkStatsManager = context.getSystemService<NetworkStatsManager>()
        totalDeviceRx = 0L
        totalDeviceTx = 0L
        Logger.debug("Get data usage information from $startTime to $endTime")
        return try {
            val networkStats = networkStatsManager?.querySummary(
                NetworkCapabilities.TRANSPORT_CELLULAR,
                null,
                startTime,
                endTime,
            )
            val bucket = NetworkStats.Bucket()
            val appDataUsageMap = mutableMapOf<String, Long>()
            networkStats?.use {
                Logger.debug("NetworkStats bucket is available to get data usage info: ${networkStats.hasNextBucket()}")
                while (it.hasNextBucket()) {
                    try {
                        it.getNextBucket(bucket)
                        totalDeviceRx += bucket.rxBytes
                        totalDeviceTx += bucket.txBytes
                        val uid = bucket.uid
                        val packageName = getPackageNameFromUid(context, uid)
                        if (DataUsageAppPackageName.packageNames.contains(packageName)) {
                            Logger.info("AppName: $packageName, Rx bytes: ${bucket.rxBytes}, Tx bytes: ${bucket.txBytes}")
                            val totalUsage = bucket.rxBytes + bucket.txBytes
                            appDataUsageMap[packageName] =
                                appDataUsageMap.getOrDefault(packageName, 0L) + totalUsage
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        Logger.warn("Error getting app info: ${e.message}", e)
                    }
                }
            }
            Logger.debug("Total sent bytes: $totalDeviceTx and total received bytes: $totalDeviceRx")
            appDataUsageMap
        } catch (e: RemoteException) {
            Logger.warn("Exception occurred in querying network stats: ${e.message}", e)
            emptyMap()
        }
    }
}