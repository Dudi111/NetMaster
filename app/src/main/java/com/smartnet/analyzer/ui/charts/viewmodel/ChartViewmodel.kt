package com.smartnet.analyzer.ui.charts.viewmodel

import androidx.lifecycle.ViewModel
import com.smartnet.analyzer.data.DataUsageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChartViewmodel @Inject constructor(
    private val dataUsageHelper: DataUsageHelper,
): ViewModel() {


}