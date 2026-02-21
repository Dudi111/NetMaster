package com.smartnet.analyzer.ui

import ComposeSpeedTestTheme
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkColor
import com.smartnet.analyzer.data.BottomNavItem
import com.smartnet.analyzer.ui.common.NetMasterScreen
import com.smartnet.analyzer.ui.common.RoundCornerDialogView
import com.smartnet.analyzer.ui.common.ScreenId.CHART_SCREEN
import com.smartnet.analyzer.ui.common.ScreenId.DATA_USAGE
import com.smartnet.analyzer.ui.common.ScreenId.SPEED_TEST
import com.smartnet.analyzer.ui.navHost.NetMasterScreenHolder
import com.smartnet.analyzer.utils.GlobalFunctions.hasUsageAccess
import com.smartnet.analyzer.utils.GlobalFunctions.navigateToScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    var dialogState = mutableStateOf(false)

    val items = listOf(
        BottomNavItem(R.drawable.person, CHART_SCREEN),
        BottomNavItem(R.drawable.speed, SPEED_TEST),
        BottomNavItem(R.drawable.wifi, DATA_USAGE)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogFeast.debug("MainActivity: onCreate")
        enableEdgeToEdge()
        hideSystemUI(this)
        setContent {
            ComposeSpeedTestTheme {
                navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationView()
                    }
                ) { paddingValues ->

                    Surface(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        NetMasterScreenHolder(navController!!)
                    }
                }
                DialogInit()
            }
        }
    }

    @Composable
    fun NavigationView() {
        val navBackStackEntry by navController!!.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        NavigationBar(
            containerColor = DarkColor
        ) {
            items.forEachIndexed { index, item ->
                val selected = currentRoute == item.route
                LogFeast.debug("Current route: $currentRoute, selected: $selected")
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        onIconClick(index)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Gray,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = Color.Gray
                    )
                )
            }
        }
    }

    /**
     * onIconClick: This method is used to navigate to different screens
     */
    fun onIconClick(index: Int) {
        when(index) {
            0 -> {
                if (!hasUsageAccess(this)) {
                    dialogState.value = true
                } else {
                    LogFeast.debug("Navigate to chart Screen")
                    navController!!.navigateToScreen(NetMasterScreen.ChartScreen.route)
                }
            }

            1 -> {
                LogFeast.debug("Navigate to speed test Screen")
                navController!!.navigateToScreen(NetMasterScreen.SpeedTestScreen.route)
            }

            2 -> {
                if (!hasUsageAccess(this)) {
                    dialogState.value = true
                } else {
                    LogFeast.debug("Navigate to data usage Screen")
                    navController!!.navigateToScreen(NetMasterScreen.DataUsageScreen.route)
                }
            }
        }
    }

    /**
     * dialogInit: This method is used to display dialog
     */
    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun DialogInit() {
        if (dialogState.value) {
            LogFeast.debug("Show data usage perm dialog")
            RoundCornerDialogView(
                R.string.data_usage_perm,
                R.string.setting_btn,
                R.string.btn_cancel,
                dialogState,
                mutableStateOf(true),
                onOkClick = {
                    LogFeast.debug("Data usage perm dialog ok pressed")
                    requestUsageAccess(this)
                    dialogState.value = false
                },
                onCancelClick = {
                    LogFeast.debug("Data usage dialog cancel pressed")
                    dialogState.value = false
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasUsageAccess(this)) {
            LogFeast.debug("Data usage perm not granted")
            dialogState.value = true
        } else {
            LogFeast.debug("Data usage perm granted")
            dialogState.value = false
        }
    }

    /**
     * requestUsageAccess: This method is used to request data usage access permission
     */
    fun requestUsageAccess(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * hideSystemUI: This method is used to hide system UI elements
     */
    fun hideSystemUI(activity: Activity) {
        val window = activity.window
        val controller = window.insetsController ?: return
        controller.hide(
            WindowInsets.Type.statusBars() or
                    WindowInsets.Type.navigationBars()
        )
        controller.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}