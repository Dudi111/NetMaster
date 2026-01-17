package com.smartnet.analyzer.ui.datausage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.ui.datausage.viewmodel.DataUsageViewmodel
import androidx.core.graphics.createBitmap

@Composable
fun DataUsageScreen(
     dataUsageViewmodel: DataUsageViewmodel = hiltViewModel()
) {



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient)
    ) {

        Header()
        LazyColumn {
            items(dataUsageViewmodel.dataList.size) { item ->
            AppDetailsView(dataUsageViewmodel.dataList[item])
           }
        }
    }
}

@Composable
fun AppDetailsView(dataUsage: AppDataUsage) {
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxSize()
        .border(
            width = 1.dp,
    color = Color.Gray,
    shape = RectangleShape)
    .padding(12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = dataUsage.icon!!.toImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
                .padding(5.dp)
        )

        Text(
            text = dataUsage.appName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))

        Spacer(Modifier.size(15.dp))
        Text(
            text = dataUsage.totalBytes.toString(),
            fontSize = 20.sp,
            textAlign = TextAlign.End,
            color = Color.White
        )
    }
}

@Composable
fun Header() {

    var expanded by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("Today") }

    val ranges = listOf(
        "This month",
        "This week",
        "Today",
        "Yesterday"
    )
    var networkExpanded by remember { mutableStateOf(false) }

    var selectedNetwork by remember { mutableStateOf("Mobile data") }

    val networks = listOf("Mobile data", "Wi-Fi")
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DATA USAGE",
            modifier = Modifier.padding(top = 52.dp, bottom = 16.dp)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.h6,
        )

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedRange,
                    color = Color.White
                    //style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ranges.forEach { range ->
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
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        /* ---------- NETWORK TYPE DROPDOWN ---------- */
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { networkExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedNetwork, color = Color.White)
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
                networks.forEach { network ->
                    DropdownMenuItem(
                        text = { Text(network) },
                        trailingIcon = {
                            if (network == selectedNetwork) {
                                Icon(Icons.Default.Check, null)
                            }
                        },
                        onClick = {
                            selectedNetwork = network
                            networkExpanded = false
                        }
                    )
                }
            }
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


