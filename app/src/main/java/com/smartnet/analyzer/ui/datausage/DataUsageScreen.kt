package com.smartnet.analyzer.ui.datausage

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.ui.datausage.viewmodel.DataUsageViewmodel
import androidx.core.graphics.createBitmap
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.common.dimen_100dp
import com.smartnet.analyzer.common.dimen_10dp
import com.smartnet.analyzer.common.dimen_12dp
import com.smartnet.analyzer.common.dimen_14sp
import com.smartnet.analyzer.common.dimen_15dp
import com.smartnet.analyzer.common.dimen_20dp
import com.smartnet.analyzer.common.dimen_40dp
import com.smartnet.analyzer.common.dimen_50dp
import com.smartnet.analyzer.common.dimen_90dp
import com.smartnet.analyzer.common.dimen_5dp
import com.smartnet.analyzer.common.dimen_7dp
import com.smartnet.analyzer.ui.MainActivity
import com.smartnet.analyzer.ui.common.MyProgressBar
import com.smartnet.analyzer.utils.GlobalFunctions

@Composable
fun DataUsageScreen(
     dataUsageViewmodel: DataUsageViewmodel = hiltViewModel()
) {

    val context = LocalContext.current as MainActivity
    LaunchedEffect(Unit) {
        LogFeast.debug("Data usage screen compose")
        if (GlobalFunctions.hasUsageAccess(context)) {
            dataUsageViewmodel.progressState.value = true
            dataUsageViewmodel.getDataUsage()
        } else {
            context.dialogState.value = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient)
    ) {

        Header(dataUsageViewmodel)
        if (dataUsageViewmodel.uiState.value) {
            LazyColumn {
                items(dataUsageViewmodel.dataList.size) { item ->
                    AppDetailsView(dataUsageViewmodel.dataList[item], dataUsageViewmodel)
                }
            }
        }
        MyProgressBar(dataUsageViewmodel.progressState, "Loading...")
    }
}



@Composable
fun AppDetailsView(dataUsage: AppDataUsage, dataUsageViewmodel: DataUsageViewmodel) {
    Spacer(Modifier.height(dimen_15dp))
    Row(
        modifier = Modifier.fillMaxSize()
    .padding(dimen_12dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = dataUsage.icon!!.toImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(dimen_40dp)
                .padding(dimen_5dp)
        )

        Text(
            text = dataUsage.appName,
            fontSize = dimen_14sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))

        Spacer(Modifier.size(dimen_15dp))
        Text(
            text = dataUsageViewmodel.formatBytes(dataUsage.totalBytes),
            fontSize = dimen_14sp,
            textAlign = TextAlign.End,
            color = Color.White
        )
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = Color.Gray.copy(alpha = 0.4f),
        modifier = Modifier.padding(horizontal = dimen_20dp)
    )
}

/**
 * Header: This method contains view of screen header
 */
@Composable
fun Header(
    dataUsageViewmodel: DataUsageViewmodel
) {

    var expanded by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(dataUsageViewmodel.dateRanges[2]) }
    var networkExpanded by remember { mutableStateOf(false) }
    var selectedNetwork by remember { mutableStateOf(dataUsageViewmodel.networkType[0]) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DATA USAGE",
            modifier = Modifier.padding(top = dimen_50dp, bottom = dimen_15dp)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = dimen_5dp),
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimen_20dp))
                        .clickable { expanded = true }
                        .padding(start = dimen_12dp, end = dimen_7dp, top = dimen_5dp, bottom = dimen_5dp)
                        .width(dimen_100dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedRange,
                        color = Color.White,
                        fontSize = dimen_14sp
                    )
                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    dataUsageViewmodel.dateRanges.forEach { range ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = range,
                                    color = Color.White
                                )
                            },
                            trailingIcon = {
                                if (range == selectedRange) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            onClick = {
                                selectedRange = range
                                expanded = false
                                dataUsageViewmodel.onDateNetworkChange(range, selectedNetwork)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(dimen_5dp))

            /* ---------- Network Type Dropdown ---------- */
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimen_20dp))
                        .clickable { networkExpanded = true }
                        .padding(horizontal = dimen_5dp, vertical = dimen_5dp)
                        .width(dimen_90dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedNetwork,
                        color = Color.White,
                        fontSize = dimen_14sp
                    )
                    Icon(
                        imageVector = if (networkExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = networkExpanded,
                    onDismissRequest = { networkExpanded = false }
                ) {
                    dataUsageViewmodel.networkType.forEach { network ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                network,
                                    color = Color.White
                            ) },
                            trailingIcon = {
                                if (network == selectedNetwork) {
                                    Icon(Icons.Default.Check, null)
                                }
                            },
                            onClick = {
                                selectedNetwork = network
                                networkExpanded = false
                                dataUsageViewmodel.onDateNetworkChange(selectedRange,network)
                            }
                        )
                    }
                }
            }

            Text(
                "Total: ${dataUsageViewmodel.totalUsage}",
                color = Color.White,
                modifier = Modifier.padding(start = dimen_12dp, top = dimen_10dp, bottom = dimen_10dp, end = dimen_5dp),
                fontSize = dimen_14sp
            )
        }
    }
}

fun Drawable.toImageBitmap(): ImageBitmap {
    val bitmap =
        createBitmap(intrinsicWidth.takeIf { it > 0 } ?: 1, intrinsicHeight.takeIf { it > 0 } ?: 1)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}