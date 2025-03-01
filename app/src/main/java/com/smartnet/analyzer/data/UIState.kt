package com.smartnet.analyzer.data

data class UIState(
    val speed: String,
    val ping: String,
    val maxSpeed: String,
    val arcValue: Float = 0f,
    val inProgress: Boolean = false
)
