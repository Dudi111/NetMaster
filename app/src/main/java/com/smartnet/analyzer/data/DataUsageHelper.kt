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
import kotlin.use

class DataUsageHelper @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun getAppDataUsage(
        startTime: Long,
        endTime: Long,
        networkType: Int
    ): List<AppDataUsage> {
        val networkStatsManager = context.getSystemService<NetworkStatsManager>()
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
                        Log.d("dudi","Error while getting buckets: $e")
                    }
                }
                it.close()
            }

            val appList = mutableListOf<AppDataUsage>()

            usageMap.forEach { (uid, bytes) ->
                try {
                    if (packageManager.getPackagesForUid(uid).isNullOrEmpty()){
                        when(uid) {
                            Process.SYSTEM_UID -> {
                                appList.add(
                                    AppDataUsage(
                                        packageName = "",
                                        appName = "System and Root",
                                        icon = ContextCompat.getDrawable(context, R.drawable.ic_setting),
                                        rxBytes = bytes.first,
                                        txBytes = bytes.second
                                    )
                                )
                                return@forEach
                            }

                            -5 -> {
                                appList.add(
                                    AppDataUsage(
                                        packageName = "",
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
                                        packageName = "",
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
                                Log.d("dudi","Blank UID: $uid , total bytes: ${bytes.first + bytes.second}")
                                return@forEach
                            }
                        }
                    }
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
            Log.d("dudi","Error while getting data usage: $e")
            emptyList()
        }
    }
}