package com.smartnet.analyzer.ui

import ComposeSpeedTestTheme
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkColor
import com.smartnet.analyzer.common.theme.Pink
import com.smartnet.analyzer.ui.common.NetMasterScreen
import com.smartnet.analyzer.ui.navHost.NetMasterScreenHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private var navController: NavHostController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            }
        }
    }

    @Composable
    fun NavigationView() {
        val items = listOf(
            R.drawable.wifi,
            R.drawable.person,
            R.drawable.speed,
            R.drawable.settings
        )
        var selectedItem by rememberSaveable { mutableStateOf(2) }

        BottomNavigation(backgroundColor = DarkColor) {
            items.mapIndexed { index, item ->
                BottomNavigationItem(selected = index == selectedItem,
                    onClick = {
                        selectedItem = index
                        onIconClick(index)
                    },
                    selectedContentColor = Pink,
                    unselectedContentColor = MaterialTheme.colors.onSurface,
                    icon = {
                        Icon(painterResource(id = item), null)
                    }
                )
            }
        }
    }

    fun onIconClick(index: Int) {
        when(index) {
            1 -> {
                navController!!.navigate(NetMasterScreen.DataUsageScreen.route)
            }

            2 -> {
                navController!!.navigate(NetMasterScreen.SpeedTestScreen.route)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasUsageAccess(this)) {
            requestUsageAccess(this)
        }
    }

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageAccess(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}