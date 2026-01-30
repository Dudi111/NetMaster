package com.smartnet.analyzer.data

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.os.Process
import android.os.RemoteException
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.smartnet.analyzer.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataUsageHelper @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val networkStatsManager = context.getSystemService<NetworkStatsManager>()

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
                        Log.d("dudi", "Error while getting buckets: $e")
                    }
                }
                it.close()
            }

            val appList = mutableListOf<AppDataUsage>()

            usageMap.forEach { (uid, bytes) ->
                try {
                    if (packageManager.getPackagesForUid(uid).isNullOrEmpty()) {
                        when (uid) {
                            Process.SYSTEM_UID -> {
                                appList.add(
                                    AppDataUsage(
                                        uid = uid,
                                        appName = "System and Root",
                                        icon = ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_setting
                                        ),
                                        rxBytes = bytes.first,
                                        txBytes = bytes.second
                                    )
                                )
                                return@forEach
                            }

                            -5 -> {
                                appList.add(
                                    AppDataUsage(
                                        uid = uid,
                                        appName = "Tethering & Hotspot",
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
                                        appName = "Removed UID usage",
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

                            else -> {
                                Log.d(
                                    "dudi",
                                    "Blank UID: $uid , total bytes: ${bytes.first + bytes.second}"
                                )
                                return@forEach
                            }
                        }
                    }
                    val packages = packageManager.getPackagesForUid(uid) ?: return@forEach
                    for (pkg in packages) {
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
                    Log.d("dudi", "Error while getting names: $e")
                }
            }
            return appList.sortedByDescending { it.totalBytes }
        } catch (e: RemoteException) {
            Log.d("dudi", "Error while getting data usage: $e")
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
            Log.d("dudi", "Error while getting data usage: $e")
            return totalUsage
        }
    }

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