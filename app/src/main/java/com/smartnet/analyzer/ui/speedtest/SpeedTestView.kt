package com.smartnet.analyzer.ui.speedtest

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.common.theme.Green500
import com.smartnet.analyzer.common.theme.GreenGradient
import com.smartnet.analyzer.common.theme.LightColor
import com.smartnet.analyzer.data.UIState
import com.smartnet.analyzer.ui.speedtest.viewmodel.SpeedTestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.floor

suspend fun startDynamicAnimation(
    animation: Animatable<Float, AnimationVector1D>,
    maxSpeed: MutableStateFlow<String>,
    ping: MutableStateFlow<String>,
    currentSpeed: MutableStateFlow<String>,
    floatValue: MutableStateFlow<Float>
) {
  //  animation.animateTo(1f) // Normalize animation progress
    val speedPerLine = 0.5f
    val totalBigLines = 9
    val maxGaugeSpeed = (totalBigLines - 1) * speedPerLine // 4.0 Mbps

    while (true) {

        val speedMbps = currentSpeed.value.toFloatOrNull() ?: 0f

        // Clamp speed to gauge limit
        val clampedSpeed = speedMbps.coerceIn(0f, maxGaugeSpeed)

        // Normalize for animation (0f → 1f)
        val normalizedProgress =
            (clampedSpeed / maxGaugeSpeed).coerceIn(0f, 1f)

        // Expose to UI if needed
        floatValue.value = normalizedProgress

        // Animate smoothly to correct tick
        animation.animateTo(
            targetValue = normalizedProgress,
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )

        delay(300)
    }
}

fun Animatable<Float, AnimationVector1D>.toUiState(
    maxSpeed: String,
    currentSpeed: String,
    ping: String
) = UIState(
    arcValue = value,
    speed = currentSpeed,
    ping = "$ping ms",
    maxSpeed = "$maxSpeed Mbps",
    inProgress = false

)

@Composable
fun SpeedTestScreenMain(
    speedTestViewModel: SpeedTestViewModel = hiltViewModel()
) {

    val animation = remember { Animatable(0f) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            startDynamicAnimation(animation, speedTestViewModel.maxSpeed, speedTestViewModel.ping, speedTestViewModel.currentSpeed, speedTestViewModel.floatValue)
        }
    }

    val uiState = animation.toUiState(speedTestViewModel.maxSpeed.value, speedTestViewModel.currentSpeed.value, speedTestViewModel.ping.value)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGradient),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Header()
            SpeedIndicator(state = uiState, speedTestViewModel = speedTestViewModel)
            AdditionalInfo(uiState.ping, uiState.maxSpeed)
        }
}

@Composable
fun Header() {
    Text(
        text = "SPEEDTEST",
        modifier = Modifier.padding(top = 52.dp, bottom = 16.dp),
        style = MaterialTheme.typography.h6
    )
}

@Composable
fun SpeedIndicator(state: UIState, speedTestViewModel: SpeedTestViewModel) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(state.arcValue, 240f)
        StartButton(!state.inProgress, speedTestViewModel)
        SpeedValue(state.speed)
    }
}

@Composable
fun SpeedValue(value: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DOWNLOAD", style = MaterialTheme.typography.caption)
        Text(
            text = value,
            fontSize = 45.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text("mbps", style = MaterialTheme.typography.caption)
    }
}

@Composable
fun StartButton(isEnabled: Boolean, speedTestViewModel: SpeedTestViewModel) {
    OutlinedButton(
        onClick = { speedTestViewModel.onStartClick() },
        modifier = Modifier.padding(bottom = 24.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colors.onSurface),

        ) {
        Text(
            text = "START",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AdditionalInfo(ping: String, maxSpeed: String) {

    @Composable
    fun RowScope.InfoColumn(title: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(title)
            Text(
                value,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        InfoColumn(title = "PING", value = ping)
        VerticalDivider()
        InfoColumn(title = "MAX SPEED", value = maxSpeed)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF414D66))
            .width(1.dp)
    )
}

@Composable
fun CircularSpeedIndicator(value: Float, angle: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        drawLines(value, angle)
        drawArcs(value, angle)
    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress

    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    fun drawStroke() {
        drawArc(
            color = Green500,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 86f, cap = StrokeCap.Round)
        )
    }

    fun drawGradient() {
        drawArc(
            brush = GreenGradient,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f, cap = StrokeCap.Round)
        )
    }

    drawStroke()
    drawGradient()
}

fun DrawScope.drawLines(progress: Float, maxValue: Float, numberOfLines: Int = 40) {
    val oneRotation = maxValue / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt() + 1

    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                LightColor,
                Offset(if (i % 5 == 0) 80f else 30f, size.height / 2),
                Offset(0f, size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}