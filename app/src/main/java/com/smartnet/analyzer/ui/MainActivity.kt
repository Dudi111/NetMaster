package com.smartnet.analyzer.ui

import ComposeSpeedTestTheme
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dude.logfeast.logs.CustomLogUtils.LogFeast
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkColor
import com.smartnet.analyzer.ui.common.NetMasterScreen
import com.smartnet.analyzer.ui.common.RoundCornerDialogView
import com.smartnet.analyzer.ui.navHost.NetMasterScreenHolder
import com.smartnet.analyzer.utils.GlobalFunctions.hasUsageAccess
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    var dialogState = mutableStateOf(false)

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
                DialogInit(navController!!)
            }
        }
    }

    @Composable
    fun NavigationView() {
        val items = listOf(
            R.drawable.person,
            R.drawable.speed,
            R.drawable.wifi
        )
        var selectedItem by rememberSaveable { mutableIntStateOf(1) }
        NavigationBar(
            containerColor = DarkColor
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = index == selectedItem,
                    onClick = {
                        selectedItem = index
                        onIconClick(index)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item),
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
                LogFeast.debug("Navigate to chart Screen")
                navController!!.navigate(NetMasterScreen.ChartScreen.route)
            }

            1 -> {
                LogFeast.debug("Navigate to speed test Screen")
                navController!!.navigate(NetMasterScreen.SpeedTestScreen.route)
            }

            2 -> {
                LogFeast.debug("Navigate to data usage Screen")
                navController!!.navigate(NetMasterScreen.DataUsageScreen.route)
            }
        }
    }

    /**
     * dialogInit: This method is used to display dialog
     */
    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun DialogInit(navController: NavHostController) {
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
                    navController.navigate(NetMasterScreen.SpeedTestScreen.route)
                    dialogState.value = false
                }
            )
        }
    }

    override fun onResume() {
        if (!hasUsageAccess(this)) {
            LogFeast.debug("Data usage perm not granted")
            dialogState.value = true
        } else {
            LogFeast.debug("Data usage perm granted")
            dialogState.value = false
        }
        super.onResume()
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