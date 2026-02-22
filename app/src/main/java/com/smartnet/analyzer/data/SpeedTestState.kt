package com.smartnet.analyzer.data

import com.smartnet.analyzer.utils.SpeedTestConstants.SPEED_TEST_CONNECTING
import com.smartnet.analyzer.utils.SpeedTestConstants.SPEED_TEST_START
import com.smartnet.analyzer.utils.SpeedTestConstants.SPEED_TEST_STOP

enum class SpeedTestState(val buttonText: String) {
    IDLE(SPEED_TEST_START),
    CONNECTING(SPEED_TEST_CONNECTING),
    RUNNING(SPEED_TEST_STOP)
}