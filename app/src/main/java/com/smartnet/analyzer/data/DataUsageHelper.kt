package com.smartnet.analyzer.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.NetworkCapabilities
import android.os.RemoteException
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.use

class DataUsageHelper @Inject constructor(
    @ApplicationContext val context: Context
) {

    private var totalDeviceRx = 0L
    private var totalDeviceTx = 0L

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
            emptyMap()
        }
    }

    private fun getPackageNameFromUid(context: Context, uid: Int): String {
        val packageManager = context.packageManager

        // Retrieve package names associated with the UID
        val packageNames = packageManager.getPackagesForUid(uid)

        // Use the first package name if available, otherwise return an empty string
        return packageNames?.getOrNull(0) ?: ""
    }
}