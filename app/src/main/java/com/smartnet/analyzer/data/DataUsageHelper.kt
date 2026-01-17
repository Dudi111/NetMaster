package com.smartnet.analyzer.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import android.os.RemoteException
import android.util.Log
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.use

class DataUsageHelper @Inject constructor(
    @ApplicationContext val context: Context
) {

    private var totalDeviceRx = 0L
    private var totalDeviceTx = 0L

    fun getAppDataUsage(
        startTime: Long,
        endTime: Long
    ): List<AppDataUsage> {
        val networkStatsManager = context.getSystemService<NetworkStatsManager>()
        val packageManager = context.packageManager
        totalDeviceRx = 0L
        totalDeviceTx = 0L
      //  Logger.debug("Get data usage information from $startTime to $endTime")
        return try {
            val networkStats = networkStatsManager?.querySummary(
                NetworkCapabilities.TRANSPORT_CELLULAR,
                null,
                startTime,
                endTime,
            )
            val bucket = NetworkStats.Bucket()
            val appDataUsageMap = mutableMapOf<String, Long>()
            val usageMap = mutableMapOf<Int, Pair<Long, Long>>()
            networkStats?.use {
              //  Logger.debug("NetworkStats bucket is available to get data usage info: ${networkStats.hasNextBucket()}")
                while (it.hasNextBucket()) {
                    try {
                        it.getNextBucket(bucket)
                        totalDeviceRx += bucket.rxBytes
                        totalDeviceTx += bucket.txBytes
                        val uid = bucket.uid
                        Log.d("dudi","UID: $uid")
                        val prev = usageMap[uid] ?: (0L to 0L)
                        usageMap[uid] = Pair(
                            prev.first + bucket.rxBytes,
                            prev.second + bucket.txBytes
                        )
//                        val packageName = getPackageNameFromUid(context, uid)
//                        val totalUsage = bucket.rxBytes + bucket.txBytes
//                        appDataUsageMap[packageName] =
//                            appDataUsageMap.getOrDefault(packageName, 0L) + totalUsage
                    } catch (e: PackageManager.NameNotFoundException) {
                    }
                }
                it.close()
            }

            val appList = mutableListOf<AppDataUsage>()

            usageMap.forEach { (uid, bytes) ->
                try {
                    Log.d("dudi", "get pkg is null: ${packageManager.getPackagesForUid(uid)}")
                    val packages = packageManager.getPackagesForUid(uid) ?: return@forEach
                    for (pkg in packages) {
                        val appInfo = packageManager.getApplicationInfo(pkg, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo)
                      //  Log.d("dudi","app info: $appInfo , name: $appName , and icon: $icon")

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
        } catch (e: RemoteException) {
            emptyList()
        }
    }

    private fun getPackageNameFromUid(context: Context, uid: Int): String {
        val packageManager = context.packageManager

        // Retrieve package names associated with the UID
        val packageNames = packageManager.getPackagesForUid(uid)

        // Use the first package name if available, otherwise return an empty string
        return packageNames?.getOrNull(0) ?: ""
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            bytes >= gb -> String.format("%.1f GB", bytes / gb)
            bytes >= mb -> String.format("%.1f MB", bytes / mb)
            bytes >= kb -> String.format("%.1f KB", bytes / kb)
            else -> "$bytes B"
        }
    }
}