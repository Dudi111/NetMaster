package com.smartnet.analyzer.ui.datausage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.ui.datausage.viewmodel.DataUsageViewmodel

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
    Text(
        text = "DATA USAGE",
        modifier = Modifier.padding(top = 52.dp, bottom = 16.dp),
        style = MaterialTheme.typography.h6
    )
}

fun Drawable.toImageBitmap(): ImageBitmap {
    val bitmap = Bitmap.createBitmap(
        intrinsicWidth.takeIf { it > 0 } ?: 1,
        intrinsicHeight.takeIf { it > 0 } ?: 1,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}


