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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
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
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkColor2
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.common.theme.LightDarkColor
import com.smartnet.analyzer.common.theme.white
import com.smartnet.analyzer.ui.charts.viewmodel.ChartViewmodel
import com.smartnet.analyzer.ui.datausage.toImageBitmap
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_CELLULAR
import com.smartnet.analyzer.utils.Constants.NETWORK_TYPE_WIFI

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
        val modelProducer = remember { CartesianChartModelProducer() }
        val modelProducer2 = remember { CartesianChartModelProducer() }
        val modelProducer3 = remember { CartesianChartModelProducer() }
        var selectedNetwork by remember { mutableStateOf(NETWORK_TYPE_CELLULAR) }
        var selectedApp by remember { chartViewmodel.selectedApp }

        LaunchedEffect(
                selectedNetwork,
            selectedApp
        ) {
            Log.d("selectedapp","third value: ${selectedApp.third}")
            modelProducer.runTransaction {
                columnSeries {
                    series(chartViewmodel.dailyDataUsage)
                }
            }

            val data: List<Float> = when (selectedNetwork) {
                NETWORK_TYPE_CELLULAR -> chartViewmodel.updateNetworkUsage(selectedNetwork)
                NETWORK_TYPE_WIFI -> chartViewmodel.updateNetworkUsage(selectedNetwork)
                else -> emptyList()
            }

            modelProducer2.runTransaction {
                columnSeries {
                    series(data)
                }
            }

            if (selectedApp.third != 0) {
                val data2 = chartViewmodel.getAppDataUsage(selectedApp.third)
                modelProducer3.runTransaction {
                    columnSeries {
                        series(data2)
                    }
                }
            }
        }

        Header()

        LazyColumn {
            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 7.dp, horizontal = 7.dp)
                        .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
                ) {

                    Column {

                        // 🔹 Title (changes with page)
                        Text(
                            text = if (pagerState.currentPage == 0)
                                "This month usage (Wi-Fi + Cellular)"
                            else
                                "Last month usage (Wi-Fi + Cellular)",
                            modifier = Modifier.padding(7.dp),
                            color = white,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )

                        // 🔹 Sliding Charts
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) { page ->

                            when (page) {
                                0 -> {
                                    // ✅ This month chart
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
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                1 -> {
                                    // ✅ Last month chart
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
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        // 🔹 Pager Indicator (Dots)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(2) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                        .background(
                                            color = if (pagerState.currentPage == index)
                                                Color.White
                                            else
                                                Color.Gray,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .wrapContentHeight()
//                        .padding(vertical = 7.dp, horizontal = 7.dp)
//                        .background(color = LightDarkColor, shape = RoundedCornerShape(10.dp))
//                        .border(1.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp)),
//                    contentAlignment = Alignment.TopCenter
//                ) {
//
//                    Text(
//                        text = "This month usage (wifi + cellular)",
//                        modifier = Modifier.padding(7.dp),
//                        style = MaterialTheme.typography.body1,
//                        color = white,
//                        fontSize = 10.sp,
//                        textAlign = TextAlign.Center
//                    )
//                    CartesianChartHost(
//                        chart = rememberCartesianChart(
//                            rememberColumnCartesianLayer(),
//                            startAxis = VerticalAxis.rememberStart(
//                                valueFormatter = { _, value, _ ->
//                                    if (value >= 1024)
//                                        String.format("%.1f GB", value / 1024f)
//                                    else
//                                        "${value.toInt()} MB"
//                                }
//                            ),
//                            bottomAxis = HorizontalAxis.rememberBottom(
//                                valueFormatter = { _, value, _ ->
//                                    "Day ${(value.toInt() + 1)}"
//                                }
//                            )
//                        ),
//                        modelProducer = modelProducer,
//                        scrollState = rememberVicoScrollState(scrollEnabled = false),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(280.dp)
//                            .padding(top = 20.dp)
//                    )
//                }
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
                            chartViewmodel.getNetworkType(it)
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
                        modelProducer = modelProducer2,
                        scrollState = rememberVicoScrollState(scrollEnabled = false),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(top = 10.dp)
                    )
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
                            modifier = Modifier.padding(3.dp)
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
                            modifier = Modifier.padding(start = 5.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                if (selectedApp.third != 0) {
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
                                        "Day ${(value.toInt() + 1)}"
                                    }
                                )
                            ),
                            modelProducer = modelProducer3,
                            scrollState = rememberVicoScrollState(scrollEnabled = false),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .padding(top = 10.dp)
                        )
                    }
                }
            }
        }
        DialogInit(chartViewmodel)
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
    chartViewmodel: ChartViewmodel
) {
    if (chartViewmodel.dialogState.value) {
        AppSelectionDialog(
            chartViewmodel = chartViewmodel,
            selectedApp = chartViewmodel.userAppList[0].third,
            onConfirm = {
                chartViewmodel.selectedApp.value = chartViewmodel.userAppList[it]
                chartViewmodel.dialogState.value = false
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
    selectedApp: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val selected = remember { chartViewmodel.selectedAppIndex }
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
                    items(chartViewmodel.userAppList.size) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                               // .clickable { onAppSelected(app) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected.value == app,
                                onClick = { chartViewmodel.selectedAppIndex.value = app },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF0A66FF),
                                    unselectedColor = Color.Gray
                                )
                            )

                            Text(
                                text = chartViewmodel.userAppList[app].second,
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
                        onClick = { onConfirm(selected.value) },
                        enabled = selectedApp != 0
                    ) {
                        Text("OK", color = Color(0xFF0A66FF))
                    }
                }
            }
        }
    }
}