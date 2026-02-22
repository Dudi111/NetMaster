package com.smartnet.analyzer.utils

object Constants {
    const val DATA_USAGE_THIS_MONTH = "This month"
    const val DATA_USAGE_THIS_WEEK = "This week"
    const val DATA_USAGE_TODAY = "Today"
    const val DATA_USAGE_YESTERDAY = "Yesterday"

    //Network type
    const val NETWORK_TYPE_CELLULAR = "Cellular"
    const val NETWORK_TYPE_WIFI = "Wi-Fi"
}

/**
 * SpeedTestConstants: This object contains all the constants related to speed test screen
 */
object SpeedTestConstants {
    const val SPEED_TEST_START = "START"
    const val SPEED_TEST_CONNECTING = "connecting"
    const val SPEED_TEST_STOP = "STOP"
    const val INTERNET_ERROR = 0
    const val SPEED_TEST_ERROR = 1
}

/**
 * DataUsageInvalidPkg: This object contains all the invalid package names for data usage
 */
object DataUsageInvalidPkg {
    const val SYSTEM_UID = "System and Root"
    const val REMOVED_UID = "Removed UID usage"
    const val HOTSPOT_UID = "Tethering & Hotspot"
    const val BACKGROUND_USER_UID = "Background User Apps"
}