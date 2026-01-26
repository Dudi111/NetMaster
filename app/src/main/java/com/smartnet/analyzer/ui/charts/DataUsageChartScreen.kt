package com.smartnet.analyzer.ui.charts

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.common.theme.LightDarkColor
import com.smartnet.analyzer.common.theme.white
import com.smartnet.analyzer.ui.charts.viewmodel.ChartViewmodel
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_CELLULAR
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_WIFI
import java.time.ZoneId
import java.time.ZonedDateTime

@SuppressLint("DefaultLocale")
@Composable
fun DataUsageChartScreen(
    chartViewmodel: ChartViewmodel = hiltViewModel()
) {

    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val pagerState = rememberPagerState(pageCount = { 2 })
        val modelProducer2 = remember { CartesianChartModelProducer() }
        val modelProducer3 = remember { CartesianChartModelProducer() }
        var selectedNetwork by remember { mutableStateOf(NETWORK_TYPE_CELLULAR) }

        val appWiseUsageState by chartViewmodel.appWiseDataUsage.collectAsState()
        val networkUsageState by chartViewmodel.networkDataUsage.collectAsState()

        LaunchedEffect(
            appWiseUsageState,
        ) {
            Log.d("launched effect","model producer 3 run")
            if (chartViewmodel.selectedAppIndex.value != -1) {
                modelProducer3.runTransaction {
                    columnSeries {
                        series(chartViewmodel.appWiseDataUsage.value)
                    }
                }
            }
        }

        LaunchedEffect(
            networkUsageState,
        ) {
            Log.d("launched effect", "model producer 2 run")
            if (chartViewmodel.networkDataUsage.value.isNotEmpty()) {
                modelProducer2.runTransaction {
                    columnSeries {
                        series(chartViewmodel.networkDataUsage.value)
                    }
                }
            }
        }
//        LaunchedEffect(
//            overallUsageState
//        ) {
//            Log.d("launched effect", "model producer 1 run")
//            if (chartViewmodel.thisMonthOverallDatalUsage.value.isNotEmpty()) {
//                modelProducer.runTransaction {
//                    lineSeries {
//                        series(overallUsageState)
//                    }
//                }
//            }
//        }

//        LaunchedEffect(
//            lastMonthUsageState
//        ) {
//            Log.d("launched effect", "model producer 1.5 run")
//            if (chartViewmodel.lastMonthOverallDatalUsage.value.isNotEmpty()) {
//                modelProducer.runTransaction {
//                    lineSeries {
//                        series(chartViewmodel.lastMonthOverallDatalUsage.value)
//                    }
//                }
//            }
//        }

        LaunchedEffect(pagerState.currentPage) {
            when(pagerState.currentPage) {
                0 -> {
                    chartViewmodel.loadThisMonthOverallUsage()
                }
                1 -> {
                    chartViewmodel.loadLastMonthOverallUsage()
                }
            }
        }

        LazyColumn {
            item {
                Header(chartViewmodel)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 7.dp, horizontal = 7.dp)
                        .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
                ) {

                    Column {

                        // 🔹 Sliding Charts
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) { page ->

                            when (page) {
                                0 -> {
                                    // This month chart

                                    CartesianChartHost(
                                        chart = rememberCartesianChart(
                                            rememberLineCartesianLayer(
                                                lineProvider = LineCartesianLayer.LineProvider.series(
                                                    LineCartesianLayer.Line(
                                                        fill = LineCartesianLayer.LineFill.single(
                                                            fill(Color(0xFF1E88E5)) // Material Blue 600
                                                        ),
                                                        areaFill = LineCartesianLayer.AreaFill.single(
                                                            fill(Color(0xFF1E88E5).copy(alpha = 0.15f))
                                                        ),
                                                        pointConnector = LineCartesianLayer.PointConnector.cubic(),
                                                    )
                                                )
                                            ),

                                            // ─── Vertical axis ───
                                            startAxis = VerticalAxis.rememberStart(
                                                itemPlacer = VerticalAxis.ItemPlacer.step(
                                                    step = { 256.0 } // nice MB steps
                                                ),
                                                valueFormatter = { _, value, _ ->
                                                    when {
                                                        value >= 1024f -> String.format("%.0f GB", value / 1024f)
                                                        value > 0f -> "${value.toInt()} MB"
                                                        else -> "0"
                                                    }
                                                },
                                                guideline = null // cleaner look
                                            ),

                                            // ─── Bottom axis ───
                                            bottomAxis = HorizontalAxis.rememberBottom(
                                                labelRotationDegrees = 0f,
                                               // itemPlacer = HorizontalAxis.ItemPlacer.step(1),
                                                valueFormatter = { _, value, _ ->
                                                    "${chartViewmodel.getCurrentMonthShortName()} ${(value.toInt() + 1)}"
                                                }
                                            )
                                        ),

                                        modelProducer = chartViewmodel.modelProducer,
                                        scrollState = rememberVicoScrollState(scrollEnabled = false),

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                    )


//                                        CartesianChartHost(
//                                            chart = rememberCartesianChart(
//                                                rememberLineCartesianLayer(
//                                                    lineProvider = LineCartesianLayer.LineProvider.series(
//                                                        LineCartesianLayer.Line(
//                                                            fill = LineCartesianLayer.LineFill.single(
//                                                                fill(Color.Blue)
//                                                            ),
//                                                            pointConnector = LineCartesianLayer.PointConnector.cubic()
//                                                        )
//                                                    )
//                                                ),
//                                                startAxis = VerticalAxis.rememberStart(),
//                                                bottomAxis = HorizontalAxis.rememberBottom()
//                                            ),
//                                            modelProducer = chartViewmodel.modelProducer,
//                                            scrollState = rememberVicoScrollState(scrollEnabled = false),
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .height(260.dp)
//                                        )



//                                    CartesianChartHost(
//                                        chart = rememberCartesianChart(
//                                            rememberColumnCartesianLayer(),
//                                            startAxis = VerticalAxis.rememberStart(
//                                                valueFormatter = { _, value, _ ->
//                                                    if (value >= 1024)
//                                                        String.format("%.1f GB", value / 1024f)
//                                                    else
//                                                        "${value.toInt()} MB"
//                                                }
//                                            ),
//                                            bottomAxis = HorizontalAxis.rememberBottom(
//                                                valueFormatter = { _, value, _ ->
//                                                    "${chartViewmodel.getCurrentMonthShortName()} ${(value.toInt() + 1)}"
//                                                }
//                                            )
//                                        ),
//                                        modelProducer = modelProducer,
//                                        scrollState = rememberVicoScrollState(scrollEnabled = false),
//                                        modifier = Modifier.fillMaxSize()
//                                    )
                                }

                                1 -> {
                                    // Last month chart

                                    CartesianChartHost(
                                        chart = rememberCartesianChart(
                                            rememberLineCartesianLayer(
                                                lineProvider = LineCartesianLayer.LineProvider.series(
                                                    LineCartesianLayer.Line(
                                                        fill = LineCartesianLayer.LineFill.single(
                                                            fill(Color(0xFF1E88E5)) // Material Blue 600
                                                        ),
                                                        areaFill = LineCartesianLayer.AreaFill.single(
                                                            fill(Color(0xFF1E88E5).copy(alpha = 0.15f))
                                                        ),
                                                        pointConnector = LineCartesianLayer.PointConnector.cubic(),
                                                    )
                                                )
                                            ),

                                            // ─── Vertical axis ───
                                            startAxis = VerticalAxis.rememberStart(
                                                itemPlacer = VerticalAxis.ItemPlacer.step(
                                                    step = { 256.0 } // nice MB steps
                                                ),
                                                valueFormatter = { _, value, _ ->
                                                    when {
                                                        value >= 1024f -> String.format("%.0f GB", value / 1024f)
                                                        value > 0f -> "${value.toInt()} MB"
                                                        else -> "0"
                                                    }
                                                },
                                                guideline = null // cleaner look
                                            ),

                                            // ─── Bottom axis ───
                                            bottomAxis = HorizontalAxis.rememberBottom(
                                                labelRotationDegrees = 0f,
                                                // itemPlacer = HorizontalAxis.ItemPlacer.step(1),
                                                valueFormatter = { _, value, _ ->
                                                    "${
                                                        chartViewmodel.getCurrentMonthShortName(
                                                            ZonedDateTime.now()
                                                                .minusMonths(1)
                                                                .withDayOfMonth(1)
                                                                .toLocalDate()
                                                                .atStartOfDay(ZoneId.systemDefault())
                                                                .toInstant()
                                                                .toEpochMilli()
                                                        )
                                                    } ${(value.toInt() + 1)}"
                                                }
                                            )
                                        ),

                                        modelProducer = chartViewmodel.modelProducer,
                                        scrollState = rememberVicoScrollState(scrollEnabled = false),

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                    )

//                                    CartesianChartHost(
//                                        chart = rememberCartesianChart(
//                                            rememberColumnCartesianLayer(),
//                                            startAxis = VerticalAxis.rememberStart(
//                                                valueFormatter = { _, value, _ ->
//                                                    if (value >= 1024)
//                                                        String.format("%.1f GB", value / 1024f)
//                                                    else
//                                                        "${value.toInt()} MB"
//                                                }
//                                            ),
//                                            bottomAxis = HorizontalAxis.rememberBottom(
//                                                valueFormatter = { _, value, _ ->
//                                                    "${chartViewmodel.getCurrentMonthShortName(
//                                                        ZonedDateTime.now()
//                                                            .minusMonths(1)
//                                                            .withDayOfMonth(1)
//                                                            .toLocalDate()
//                                                            .atStartOfDay(ZoneId.systemDefault())
//                                                            .toInstant()
//                                                            .toEpochMilli())} ${(value.toInt() + 1)}"
//                                                }
//                                            )
//                                        ),
//                                        modelProducer = modelProducer,
//                                        scrollState = rememberVicoScrollState(scrollEnabled = false),
//                                        modifier = Modifier.fillMaxSize()
//                                    )
                                }
                            }
                        }

                        // 🔹 Pager Indicator (Dots)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, top = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(2) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                        .background(
                                            color = if (pagerState.currentPage == index) Color.White
                                            else Color.Gray, shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 20.dp, end = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    NetworkSwitcher(
                        selected = selectedNetwork,
                        onSelectedChange = {
                            selectedNetwork = it
                            chartViewmodel.loadNetworkUsage(it)
                        },
                        modifier = Modifier.width(150.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 7.dp, horizontal = 7.dp)
                        .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp)),
                ) {

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (selectedNetwork == NETWORK_TYPE_CELLULAR)
                                "Total Cellular usage for ${chartViewmodel.networkUsageDetail.value.month}: ${chartViewmodel.networkUsageDetail.value.total}"
                            else
                                "Total Wifi usage for ${chartViewmodel.networkUsageDetail.value.month}: ${chartViewmodel.networkUsageDetail.value.total}",
                            modifier = Modifier.padding(7.dp),
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
                                        "${chartViewmodel.getCurrentMonthShortName()} ${(value.toInt() + 1)}"
                                    }
                                )
                            ),
                            modelProducer = modelProducer2,
                            scrollState = rememberVicoScrollState(scrollEnabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(top = 10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App data usage",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 10.dp)
                    )

                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 7.dp, horizontal = 7.dp)
                        .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = chartViewmodel.selectedApp.value.first?.let {
                                BitmapPainter(it.toBitmap().asImageBitmap())
                            } ?: painterResource(R.drawable.ic_default_app),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(5.dp)
                        )

                        Text(
                            text = chartViewmodel.selectedApp.value.second,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding( start = 5.dp)
                        )

                        Icon(
                            imageVector = if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(3.dp)
                                .clickable {
                                    chartViewmodel.dialogState.value = true
                                }
                        )

                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Total: ${chartViewmodel.appWiseTotalUsage.value}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .padding(start = 5.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                if (chartViewmodel.selectedApp.value.third != 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 7.dp, horizontal = 7.dp)
                            .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
                            .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp)),
                    ) {
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
                                        "${chartViewmodel.getCurrentMonthShortName()} ${(value.toInt() + 1)}"
                                    }
                                )
                            ),
                            modelProducer = modelProducer3,
                            scrollState = rememberVicoScrollState(scrollEnabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(top = 10.dp, bottom = 10.dp)
                        )
                    }
                }
            }
        }
        DialogInit(chartViewmodel)
    }
}

@Composable
fun Header(
    chartViewmodel: ChartViewmodel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(DarkGradient),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chartViewmodel.overallUsageDetail.value.month,
            modifier = Modifier.padding(top = 50.dp, bottom = 5.dp),
            style = MaterialTheme.typography.h6
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 7.dp, horizontal = 7.dp)
                .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
                .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(LightDarkColor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = chartViewmodel.overallUsageDetail.value.total,
                    modifier = Modifier.padding(top= 10.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = white
                    )
                )

                Text(
                    text = "Total Data Used",
                    modifier = Modifier.padding(5.dp),
                    style =  TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                     //   lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = white
                    )
                )

                Text(
                    text = "WIFI + CELLULAR",
                    modifier = Modifier.padding(bottom = 10.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                       // lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = white
                    )
                )
            }
        }
    }
}

@Composable
fun NetworkSwitcher(
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
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
            .clip(RoundedCornerShape(16.dp))
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

@Composable
fun DialogInit(
    chartViewmodel: ChartViewmodel,
) {
    if (chartViewmodel.dialogState.value) {
        AppSelectionDialog(
            chartViewmodel = chartViewmodel,
            onConfirm = {
                chartViewmodel.onConfirmClick(chartViewmodel.selectedAppIndex.value)
            },
            onDismiss = {
                chartViewmodel.dialogState.value = false
            }
        )
    }
}

@Composable
fun AppSelectionDialog(
    chartViewmodel: ChartViewmodel,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp) // 🔑 makes dialog scrollable
        ) {
            Column {

                // 🔹 Title
                Text(
                    text = "Select App",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )

                Divider(color = Color.Gray.copy(alpha = 0.4f))

                // 🔹 Scrollable App List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    items(chartViewmodel.userAppList!!.size) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chartViewmodel.selectedAppIndex.value = index }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = chartViewmodel.selectedAppIndex.value == index,
                                onClick = { chartViewmodel.selectedAppIndex.value = index },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF0A66FF),
                                    unselectedColor = Color.Gray
                                )
                            )

                            Text(
                                text = chartViewmodel.userAppList!![index].appName,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.4f))

                // 🔹 Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = { onConfirm(chartViewmodel.selectedAppIndex.value) },
                        enabled = chartViewmodel.selectedAppIndex.value != -1
                    ) {
                        Text("OK", color = Color(0xFF0A66FF))
                    }
                }
            }
        }
    }
}