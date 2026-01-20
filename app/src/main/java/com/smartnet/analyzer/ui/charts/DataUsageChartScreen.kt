package com.smartnet.analyzer.ui.charts

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.ui.charts.viewmodel.ChartViewmodel

@SuppressLint("DefaultLocale")
@Composable
fun DataUsageChartScreen(
    chartViewmodel: ChartViewmodel = hiltViewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient)
    ) {

        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(chartViewmodel.dailyDataUsage) {
            modelProducer.runTransaction {
                columnSeries {
                    series(chartViewmodel.dailyDataUsage)
                }
            }
        }

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
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    }
}