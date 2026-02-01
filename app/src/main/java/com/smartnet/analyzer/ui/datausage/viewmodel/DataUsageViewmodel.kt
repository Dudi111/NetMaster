package com.smartnet.analyzer.ui.datausage.viewmodel

import android.annotation.SuppressLint
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartnet.analyzer.data.AppDataUsage
import com.smartnet.analyzer.data.DataUsageHelper
import com.smartnet.analyzer.utils.Constants
import com.smartnet.analyzer.utils.GlobalFunctions.getTimeRange
import com.smartnet.analyzer.utils.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DataUsageViewmodel @Inject constructor(
    private val dataUsageHelper: DataUsageHelper,
    @field:IoDispatcher var ioDispatcher: CoroutineDispatcher,
) : ViewModel(){


    var progressState = mutableStateOf(false)
    var uiState = mutableStateOf(false)
    var dataList = mutableListOf(AppDataUsage(icon = null, txBytes = 0L, rxBytes = 0L))

    val dateRanges = listOf(
        Constants.DATA_USAGE_THIS_MONTH,
        Constants.DATA_USAGE_THIS_WEEK,
        Constants.DATA_USAGE_TODAY,
        Constants.DATA_USAGE_YESTERDAY
    )
    val networkType = listOf(Constants.NETWORK_TYPE_CELLULAR, Constants.NETWORK_TYPE_WIFI)

    var totalUsage by mutableStateOf("")
        private set

    fun updateTotalUsage() {
        val totalBytes = dataList.sumOf { it.totalBytes }
        totalUsage = formatBytes(totalBytes)
    }

    /**
     * getDataUsage: This method is used to get data usage
     */
    fun getDataUsage() {
        val (startTime, endTime) = getTimeRange(dateRanges[2])
        viewModelScope.launch(ioDispatcher) {
            dataList = dataUsageHelper.getAppDataUsage(startTime, endTime, NetworkCapabilities.TRANSPORT_CELLULAR).toMutableList()
            updateTotalUsage()
            progressState.value = false
            uiState.value= true
        }
    }

    fun onDateNetworkChange(dateRange: String, network: String) {
        uiState.value = false
        progressState.value = true
        val (startTime, endTime) = getTimeRange(dateRange)
        viewModelScope.launch(ioDispatcher) {
            dataList = dataUsageHelper.getAppDataUsage(startTime, endTime, getNetworkType(network)).toMutableList()
            updateTotalUsage()
            progressState.value = false
            uiState.value= true
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            bytes >= gb -> String.format("%.2f GB", bytes / gb)
            bytes >= mb -> String.format("%.2f MB", bytes / mb)
            bytes >= kb -> String.format("%.2f KB", bytes / kb)
            else -> "$bytes B"
        }
    }

    fun getNetworkType(type: String): Int {
        return when(type) {
            Constants.NETWORK_TYPE_CELLULAR -> NetworkCapabilities.TRANSPORT_CELLULAR
            Constants.NETWORK_TYPE_WIFI -> NetworkCapabilities.TRANSPORT_WIFI
            else -> NetworkCapabilities.TRANSPORT_CELLULAR
        }
    }
}