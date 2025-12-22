package com.smartnet.analyzer.ui

import ComposeSpeedTestTheme
import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.smartnet.analyzer.R
import com.smartnet.analyzer.common.theme.DarkColor
import com.smartnet.analyzer.common.theme.Pink
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
        val selectedItem = 2

        BottomNavigation(backgroundColor = DarkColor) {
            items.mapIndexed { index, item ->
                BottomNavigationItem(selected = index == selectedItem,
                    onClick = { },
                    selectedContentColor = Pink,
                    unselectedContentColor = MaterialTheme.colors.onSurface,
                    icon = {
                        Icon(painterResource(id = item), null)
                    }
                )
            }
        }
    }
}