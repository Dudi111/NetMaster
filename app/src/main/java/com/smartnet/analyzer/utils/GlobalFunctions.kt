package com.smartnet.analyzer.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.navigation.NavController
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.MyApplication.Companion.mApplicationContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object GlobalFunctions {

    fun getTimeRange(
        type: String,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {

        val now = LocalDate.now(zoneId)

        val (startDateTime, endDateTime) = when (type) {

            Constants.DATA_USAGE_TODAY -> {
                val start = now.atStartOfDay(zoneId)
                val end = now.plusDays(1).atStartOfDay(zoneId).minusNanos(1)
                LogFeast.debug("Today start end time: {} , {}", start, end)
                start to end
            }

            Constants.DATA_USAGE_YESTERDAY -> {
                val yesterday = now.minusDays(1)
                val start = yesterday.atStartOfDay(zoneId)
                val end = now.atStartOfDay(zoneId).minusNanos(1)
                LogFeast.debug("Yesterday start end time: {} , {}", start, end)
                start to end
            }

            Constants.DATA_USAGE_THIS_WEEK -> {
                val startOfWeek = now.with(DayOfWeek.MONDAY)
                val start = startOfWeek.atStartOfDay(zoneId)
                val end = start.plusWeeks(1).minusNanos(1)
                LogFeast.debug("This week start end time: {} , {}", start, end)
                start to end
            }

            Constants.DATA_USAGE_THIS_MONTH -> {
                val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                val start = startOfMonth.atStartOfDay(zoneId)
                val end = start.plusMonths(1).minusNanos(1)
                LogFeast.debug("This month start time: {} ,end time {}", start, end)
                start to end
            }

            else -> {
                val start = now.atStartOfDay(zoneId)
                val end = now.plusDays(1).atStartOfDay(zoneId).minusNanos(1)
                start to end
            }
        }

        return Pair(
            startDateTime.toInstant().toEpochMilli(),
            endDateTime.toInstant().toEpochMilli()
        )
    }

    @SuppressLint("DefaultLocale")
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

    /**
     * hasUsageAccess: This method is used to check data usage access permission
     */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        LogFeast.debug("Usage access mode: $mode")
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun bytesToMb(bytes: Long): Float {
        return bytes / (1024f * 1024f)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            mApplicationContext!!.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun NavController.navigateToScreen(route: String) {
        this.navigate(route) {
            popUpTo(this@navigateToScreen.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}