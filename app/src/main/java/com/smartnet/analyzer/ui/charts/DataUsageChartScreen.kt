package com.smartnet.analyzer.ui.charts

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.smartnet.analyzer.common.theme.DarkColor2
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.common.theme.white
import com.smartnet.analyzer.ui.charts.viewmodel.ChartViewmodel
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_CELLULAR
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_WIFI

@SuppressLint("DefaultLocale")
@Composable
fun DataUsageChartScreen(
    chartViewmodel: ChartViewmodel = hiltViewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val modelProducer = remember { CartesianChartModelProducer() }
        var selectedNetwork by remember { mutableStateOf(NETWORK_TYPE_CELLULAR) }

        LaunchedEffect(chartViewmodel.dailyDataUsage) {
            modelProducer.runTransaction {
                columnSeries {
                    series(chartViewmodel.dailyDataUsage)
                }
            }
        }

        Header()

        Box(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 10.dp, horizontal = 20.dp)
                .background(color = DarkColor2, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "This month usage",
                modifier = Modifier.padding(7.dp),
                style = MaterialTheme.typography.body1,
                color = white,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = { _, value, _ ->
                            if (value >= 1024)
                                String.format("%.1f GB", value / 1024f)
                            else
                                "${value.toInt()} MB"
                        }
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            "Day ${(value.toInt() + 1)}"
                        }
                    )
                ),
                modelProducer = modelProducer,
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(5.dp)
            )
        }
        NetworkSwitcher(
            selected = selectedNetwork,
            onSelectedChange = { selectedNetwork = it },
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
fun Header() {
    Text(
        text = "Usage overview",
        modifier = Modifier.padding(top = 52.dp, bottom = 16.dp),
        style = MaterialTheme.typography.h6
    )
}

@Composable
fun NetworkSwitcher(
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF2A2A2A))
            .padding(4.dp)
    ) {
        Row {
            NetworkOption(
                text = "Cellular",
                selected = selected == NETWORK_TYPE_CELLULAR,
                onClick = { onSelectedChange(NETWORK_TYPE_CELLULAR) },
                modifier = Modifier.weight(1f)
            )
            NetworkOption(
                text = "Wi-Fi",
                selected = selected == NETWORK_TYPE_WIFI,
                onClick = { onSelectedChange(NETWORK_TYPE_WIFI) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NetworkOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF0A66FF) else Color.Transparent,
        label = "bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFFB0B0B0),
        label = "text"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}