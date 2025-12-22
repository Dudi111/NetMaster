package com.smartnet.analyzer.ui.navHost

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.smartnet.analyzer.ui.common.NetMasterScreen
import com.smartnet.analyzer.ui.speedtest.SpeedTestScreenMain

@Composable
fun NetMasterScreenHolder(
    navController : NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NetMasterScreen.SpeedTestScreen.route,
        enterTransition =  {
            fadeIn(animationSpec = tween(400))
        },
        exitTransition =  {
            fadeOut(animationSpec = tween(400))
        },
    ) {

        composable(NetMasterScreen.SpeedTestScreen.route) {
            SpeedTestScreenMain()
        }
    }
}