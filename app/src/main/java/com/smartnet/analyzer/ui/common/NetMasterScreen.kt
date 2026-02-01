package com.smartnet.analyzer.ui.common


/**
 * ScreenId: This object contains all the screen ids
 */
sealed class NetMasterScreen (
    val route: String,
    val testTag: String
) {

    object SpeedTestScreen: NetMasterScreen(route = ScreenId.SPEED_TEST,  TestTags.TEST_TAG_SPEED_TEST)
    object DataUsageScreen: NetMasterScreen(route = ScreenId.DATA_USAGE, TestTags.TEST_TAG_DATA_USAGE)
    object ChartScreen: NetMasterScreen(route = ScreenId.CHART_SCREEN, TestTags.TEST_TAG_DATA_USAGE)
}