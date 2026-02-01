package com.smartnet.analyzer.data

data class UIState(
    val currentSpeedMbps: Float = 0f,
    val maxSpeedMbps: Float = 0f,
    val pingMs: Int = 0,
    val speedometerProgress: Float = 0f, // 0f..1f
    val isRunning: Boolean = false
)
