package com.smartnet.analyzer.utils

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
                start to end
            }

            Constants.DATA_USAGE_YESTERDAY -> {
                val yesterday = now.minusDays(1)
                val start = yesterday.atStartOfDay(zoneId)
                val end = now.atStartOfDay(zoneId).minusNanos(1)
                start to end
            }

            Constants.DATA_USAGE_THIS_WEEK -> {
                val startOfWeek = now.with(DayOfWeek.MONDAY)
                val start = startOfWeek.atStartOfDay(zoneId)
                val end = start.plusWeeks(1).minusNanos(1)
                start to end
            }

            Constants.DATA_USAGE_THIS_MONTH -> {
                val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                val start = startOfMonth.atStartOfDay(zoneId)
                val end = start.plusMonths(1).minusNanos(1)
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