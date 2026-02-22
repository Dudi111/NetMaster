package com.smartnet.analyzer.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.os.Process
import android.os.RemoteException
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.R
import com.smartnet.analyzer.utils.DataUsageInvalidPkg.BACKGROUND_USER_UID
import com.smartnet.analyzer.utils.DataUsageInvalidPkg.HOTSPOT_UID
import com.smartnet.analyzer.utils.DataUsageInvalidPkg.REMOVED_UID
import com.smartnet.analyzer.utils.DataUsageInvalidPkg.SYSTEM_UID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataUsageHelper @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val networkStatsManager = context.getSystemService<NetworkStatsManager>()

    /**
     * getAppDataUsage: This method is used to get data usage of all apps
     * @param startTime: Start time in millis
     * @param endTime: End time in millis
     * @param networkType: Network type i.e. wifi/cellular
     */
    fun getAppDataUsage(
        startTime: Long,
        endTime: Long,
        networkType: Int
    ): List<AppDataUsage> {
        val packageManager = context.packageManager
        return try {
            val networkStats = networkStatsManager?.querySummary(
                networkType,
                null,
                startTime,
                endTime,
            )
            val bucket = NetworkStats.Bucket()
            val usageMap = mutableMapOf<Int, Pair<Long, Long>>()
            networkStats?.use {
                while (it.hasNextBucket()) {
                    try {
                        it.getNextBucket(bucket)
                        val uid = bucket.uid
                        val prev = usageMap[uid] ?: (0L to 0L)
                        usageMap[uid] = Pair(
                            prev.first + bucket.rxBytes,
                            prev.second + bucket.txBytes
                        )

                    } catch (e: Exception) {
                        LogFeast.error("Exception while getting data usage:", e)
                    }
                }
                it.close()
            }

            val appList = mutableListOf<AppDataUsage>()

            usageMap.forEach { (uid, bytes) ->
                try {
                    val packageInfo = packageManager.getPackagesForUid(uid)
                    if (packageInfo.isNullOrEmpty()) {
                        when (uid) {
                            in Process.SYSTEM_UID..1099 -> {
                                val existingSystemApp = appList.find { it.appName == SYSTEM_UID }

                                if (existingSystemApp != null) {
                                    // Update existing entry
                                    existingSystemApp.rxBytes += bytes.first
                                    existingSystemApp.txBytes += bytes.second
                                } else {
                                    // Create new entry
                                    appList.add(
                                        AppDataUsage(
                                            uid = uid,
                                            appName = SYSTEM_UID,
                                            icon = ContextCompat.getDrawable(
                                                context,
                                                R.drawable.ic_setting
                                            ),
                                            rxBytes = bytes.first,
                                            txBytes = bytes.second
                                        )
                                    )
                                }
                                return@forEach
                            }

                            -5 -> {
                                appList.add(
                                    AppDataUsage(
                                        uid = uid,
                                        appName = HOTSPOT_UID,
                                        icon = ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_hotspot
                                        ),
                                        rxBytes = bytes.first,
                                        txBytes = bytes.second
                                    )
                                )
                                return@forEach
                            }

                            -4 -> {
                                appList.add(
                                    AppDataUsage(
                                        uid = uid,
                                        appName = REMOVED_UID,
                                        icon = ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_delete
                                        ),
                                        rxBytes = bytes.first,
                                        txBytes = bytes.second
                                    )
                                )
                                return@forEach
                            }

                            in 10000..Int.MAX_VALUE -> {
                                val existingUserApp = appList.find { it.appName == BACKGROUND_USER_UID }

                                if (existingUserApp != null) {
                                    // Update existing entry
                                    existingUserApp.rxBytes += bytes.first
                                    existingUserApp.txBytes += bytes.second
                                } else {
                                    // Create new entry
                                    appList.add(
                                        AppDataUsage(
                                            uid = uid,
                                            appName = BACKGROUND_USER_UID,
                                            icon = ContextCompat.getDrawable(
                                                context,
                                                R.drawable.background_apps
                                            ),
                                            rxBytes = bytes.first,
                                            txBytes = bytes.second
                                        )
                                    )
                                }
                                return@forEach
                            }

                            else -> {
                                LogFeast.warn("Blank UID: $uid , total bytes: ${bytes.first + bytes.second}")
                                return@forEach
                            }
                        }
                    }
                    for (pkg in packageInfo) {
                        val appInfo = packageManager.getApplicationInfo(pkg, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo)

                        appList.add(
                            AppDataUsage(
                                uid = uid,
                                appName = appName,
                                icon = icon,
                                rxBytes = bytes.first,
                                txBytes = bytes.second
                            )
                        )
                    }
                } catch (e: Exception) {
                    LogFeast.error("Exception while getting package info:",e)
                }
            }
            return appList.sortedByDescending { it.totalBytes }
        } catch (e: RemoteException) {
            LogFeast.error("Error while getting data usage: $e")
            emptyList()
        }
    }

    fun getDayWiseDataUsage(
        startTime: Long,
        endTime: Long,
        networkType: Int
    ): Long {

        var totalUsage = 0L
        try {
            val networkStats = networkStatsManager?.querySummary(
                networkType,
                null,
                startTime,
                endTime,
            )
            val bucket = NetworkStats.Bucket()
            networkStats?.use {
                while (it.hasNextBucket()) {
                    it.getNextBucket(bucket)
                    totalUsage += bucket.rxBytes + bucket.txBytes
                }
                it.close()
            }
            return totalUsage
        } catch (e: RemoteException) {
            LogFeast.error("Error while getting day wise data usage:", e)
            return totalUsage
        }
    }

    /**
     * getUidDataUsage: This method is used to get data usage of a particular app
     * @param networkType: Network type
     * @param uid: UID of the app
     * @param startTime: Start time
     * @param endTime: End time
     */
    fun getUidDataUsage(
        networkType: Int,
        uid: Int,
        startTime: Long,
        endTime: Long
    ): Long {
        var totalBytes = 0L

        val stats = networkStatsManager!!.querySummary(
            networkType,
            null,
            startTime,
            endTime,
        )

        val bucket = NetworkStats.Bucket()
        while (stats.hasNextBucket()) {
            stats.getNextBucket(bucket)
            val uid1 = bucket.uid
            if (uid1 == uid) {
                totalBytes += bucket.rxBytes + bucket.txBytes
            }
        }
        stats.close()
        return totalBytes
    }
}