package com.smartnet.analyzer.ui.datausage.viewmodel

import com.smartnet.analyzer.data.AppDataUsage


@HiltViewModel
class DataUsageViewmodel() {

    val dataList = mutableListOf(AppDataUsage())
}