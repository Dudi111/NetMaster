package com.smartnet.analyzer.ui.datausage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.sp
import com.smartnet.analyzer.common.theme.DarkGradient
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.ui.datausage.viewmodel.DataUsageViewmodel

@Composable
fun DataUsageScreen(
     dataUsageViewmodel: DataUsageViewmodel = hiltviewmodel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient)
    ) {

        LazyColumn {
            items(dataUsageViewmodel.dataList.size) { item ->
            AppDetailsView(dataUsageViewmodel.dataList[item])
           }
        }
    }
}

@Composable
fun AppDetailsView(dataUsage: AppDataUsage) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            imageVector = dataUsage.icon,
            contentDescription = null,
        )
        Image

        Text(
            text = dataUsage.packageName,
            fontSize = 30.sp,
        )
        Text(
            text = dataUsage.usagePercentage,
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.Top)
        )
    }
}


