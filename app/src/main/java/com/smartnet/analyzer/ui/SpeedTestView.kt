package com.smartnet.analyzer.ui

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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkColor
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.common.theme.Green200
import com.smartnet.analyzer.common.theme.Green500
import com.smartnet.analyzer.common.theme.GreenGradient
import com.smartnet.analyzer.common.theme.LightColor
import com.smartnet.analyzer.common.theme.Pink
import com.smartnet.analyzer.data.UIState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

suspend fun startDynamicAnimation(
    animation: Animatable<Float, AnimationVector1D>,
    maxSpeed: MutableState<String>,
    ping: MutableState<String>,
    currentSpeed: MutableState<String>,
    floatValue: MutableState<Float>
) {
    animation.animateTo(1f) // Normalize animation progress
    while (true) {
        val speed = currentSpeed.value.toFloatOrNull() ?: 0f // Get current speed
        val maxSpeedValue = maxSpeed.value.toFloatOrNull() ?: 0f

        // Normalize speed between 0 to 1 for animation scaling
        val normalizedSpeed = if (maxSpeedValue > 0) speed / maxSpeedValue else 0f
        floatValue.value = normalizedSpeed

        animation.animateTo(normalizedSpeed, animationSpec = tween(500, easing = FastOutSlowInEasing))

        delay(500) // Update every 500ms
    }
}

fun Animatable<Float, AnimationVector1D>.toUiState(
    maxSpeed: String,
    currentSpeed: String,
    ping: String
) = UIState(
    arcValue = value,
    speed = "$currentSpeed Mbps",
    ping = "$ping ms",
    maxSpeed = "$maxSpeed Mbps",
    inProgress = false

)

@Composable
fun SpeedTestScreenMain(
    animation: Animatable<Float, AnimationVector1D>,
    maxSpeed: MutableState<String>,
    ping: MutableState<String>,
    currentSpeed: MutableState<String>,
    floatValue: MutableState<Float>,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            startDynamicAnimation(animation, maxSpeed, ping, currentSpeed, floatValue)
        }
    }

    val uiState = animation.toUiState(maxSpeed.value, currentSpeed.value, ping.value)

    SpeedTestView(
        state = uiState,
        onClick = onClick
    )
}

@Composable
fun SpeedTestView(state: UIState, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Header()
        SpeedIndicator(state = state, onClick = onClick)
        AdditionalInfo(state.ping, state.maxSpeed)
        NavigationView()
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
fun SpeedIndicator(state: UIState, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(state.arcValue, 240f)
        StartButton(!state.inProgress, onClick)
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
fun StartButton(isEnabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
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
fun NavigationView() {
    val items = listOf(
        R.drawable.wifi,
        R.drawable.person,
        R.drawable.speed,
        R.drawable.settings
    )
    val selectedItem = 2

    BottomNavigation(backgroundColor = DarkColor) {
        items.mapIndexed { index, item ->
            BottomNavigationItem(selected = index == selectedItem,
                onClick = { },
                selectedContentColor = Pink,
                unselectedContentColor = MaterialTheme.colors.onSurface,
                icon = {
                    Icon(painterResource(id = item), null)
                }
            )
        }
    }
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

//    fun drawBlur() {
//        for (i in 0..20) {
//            drawArc(
//                color = Green200.copy(alpha = i / 900f),
//                startAngle = startAngle,
//                sweepAngle = sweepAngle,
//                useCenter = false,
//                topLeft = topLeft,
//                size = size,
//                style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)
//            )
//        }
//    }

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

    //drawBlur()
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