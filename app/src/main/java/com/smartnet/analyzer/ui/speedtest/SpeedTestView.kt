package com.smartnet.analyzer.ui.speedtest

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.dimen_1dp
import com.smartnet.analyzer.common.dimen_40dp
import com.smartnet.analyzer.common.dimen_7dp
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.common.theme.Green500
import com.smartnet.analyzer.common.theme.GreenGradient
import com.smartnet.analyzer.common.theme.LightColor
import com.smartnet.analyzer.data.SpeedTestState
import com.smartnet.analyzer.ui.common.RoundCornerDialogView
import com.smartnet.analyzer.ui.speedtest.viewmodel.SpeedTestViewModel
import kotlinx.coroutines.launch
import kotlin.math.floor

@Preview
@Composable
fun SimpleComposablePreview() {
    SpeedTestScreenMain()
}

@SuppressLint("StateFlowValueCalledInComposition", "SuspiciousIndentation")
@Composable
fun SpeedTestScreenMain(
    speedTestViewModel: SpeedTestViewModel = hiltViewModel()
) {

    val uiState by speedTestViewModel.uiState.collectAsStateWithLifecycle()
    val animation = remember { Animatable(0f) }

    LaunchedEffect(uiState.speedometerProgress) {
        animation.animateTo(
            uiState.speedometerProgress,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            speedTestViewModel.scope.launch(speedTestViewModel.dispatcher) {
                try {
                    speedTestViewModel.currentInputStream?.close()
                    speedTestViewModel.currentInputStream = null
                } catch (e: Exception) {
                    LogFeast.error("Exception in closing input stream:", e)
                } finally {
                    speedTestViewModel.speedTestJob?.cancel()
                    speedTestViewModel.currentInputStream = null
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Header()
        SpeedIndicator(
            arcValue = animation.value,
            speed = uiState.currentSpeedMbps,
            speedTestViewModel = speedTestViewModel,
        )
        AdditionalInfo(
            ping = "${uiState.pingMs} ms",
            maxSpeed = "${"%.2f".format(uiState.maxSpeedMbps)} ${stringResource(R.string.mbps_text)}"
        )
        DialogInit(speedTestViewModel)
    }
}

/**
 * Header: This method is used to show the header
 */
@Composable
fun Header() {
    Text(
        text = stringResource(R.string.speed_test_header),
        modifier = Modifier.padding(top = 52.dp, bottom = 16.dp),
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White
    )
}

/**
 * SpeedIndicator: This method is used to show the speed indicator
 */
@Composable
fun SpeedIndicator(
    arcValue: Float,
    speed: Float,
    speedTestViewModel: SpeedTestViewModel,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CircularSpeedIndicator(arcValue)
        StartButton(speedTestViewModel)
        SpeedValue("%.1f".format(speed))
    }
}

/**
 * SpeedValue: This method is used to show the speed value
 */
@Composable
fun SpeedValue(value: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.download_text), style = MaterialTheme.typography.bodyLarge,
            color = Color.White)
        Text(
            text = value,
            fontSize = 45.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(stringResource(R.string.mbps_text),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White)
    }
}

/**
 * StartButton: This method is used to show the start button
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun StartButton( speedTestViewModel: SpeedTestViewModel) {
    OutlinedButton(
        onClick = { speedTestViewModel.onStartClick() },
        modifier = Modifier.padding(bottom = 24.dp),
        enabled =  speedTestViewModel.uiState.value.btnState != SpeedTestState.CONNECTING,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(width = 2.dp, color = Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.Gray
        )
        ) {
        Text(
            text = speedTestViewModel.uiState.value.btnState.buttonText,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            color = Color.Black,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * AdditionalInfo: This method is used to show the ping and max speed value
 * @param ping: ping value
 * @param maxSpeed: max speed value
 */
@Composable
fun AdditionalInfo(ping: String, maxSpeed: String) {
    @Composable
    fun RowScope.InfoColumn(title: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                color = Color.White
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = dimen_7dp),
                color = Color.White
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

/**
 * VerticalDivider: This method is used to show the vertical divider
 */
@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF414D66))
            .width(dimen_1dp)
    )
}

@Composable
fun CircularSpeedIndicator(arcValue: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimen_40dp)
    ) {
        drawLines(arcValue)
        drawArcs(arcValue)
    }
}

fun DrawScope.drawArcs(progress: Float) {
    val startAngle = 270 - 240f / 2
    val sweepAngle = 240f * progress

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

fun DrawScope.drawLines(progress: Float, numberOfLines: Int = 40) {
    val oneRotation = 240f / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt() + 1
    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - 240f) / 2) {
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

@Suppress("UnrememberedMutableState")
@Composable
fun DialogInit(speedTestViewModel: SpeedTestViewModel) {
    LaunchedEffect(speedTestViewModel.dialogState) {
        LogFeast.debug("Display dialog with ID:  ${speedTestViewModel.dialogID}")
    }
    if (speedTestViewModel.dialogState.value) {
        RoundCornerDialogView(
            speedTestViewModel.dialogMessage,
            R.string.ok,
            R.string.empty,
            speedTestViewModel.dialogState,
            mutableStateOf(false),
            onOkClick = {
                LogFeast.debug("Dialog ok button clicked, ID: {}", speedTestViewModel.dialogID)
                speedTestViewModel.dialogState.value = false
            }
        )
    }
}