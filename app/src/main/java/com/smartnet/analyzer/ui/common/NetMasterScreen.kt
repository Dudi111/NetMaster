package com.smartnet.analyzer.ui.common

import com.smartnet.analyzer.R

sealed class NetMasterScreen (
    val route: String,
    val img: Int,
    val testTag: String
) {

    object SpeedTestScreen: NetMasterScreen(route = ScreenId.SPEED_TEST, R.drawable.speed, TestTags.TEST_TAG_SPEED_TEST)
    object DataUsageScreen: NetMasterScreen(route = ScreenId.DATA_USAGE, R.drawable.wifi, TestTags.TEST_TAG_DATA_USAGE)
}