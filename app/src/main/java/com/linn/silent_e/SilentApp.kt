package com.linn.silent_e

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.linn.silent_e.ui.navigation.Screens
import com.linn.silent_e.ui.navigation.SilentNavHost

@Composable
fun SilentApp(navController: NavHostController = rememberNavController()) {
    BottomNavigationBar(navController)
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    var navigationSelectedItem by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomNavigationItem().bottomNavigationItems()
                    .forEachIndexed { index, navigationItem ->
                        NavigationBarItem(
                            selected = index == navigationSelectedItem,
                            label = {
                                Text(navigationItem.label)
                            },
                            icon = {
                                Icon(
                                    navigationItem.icon,
                                    contentDescription = navigationItem.label
                                )
                            },
                            onClick = {
                                navigationSelectedItem = index
                                navController.navigate(navigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
            }
        }
    ) { paddingValues ->
        SilentNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues = paddingValues)
        )
    }
}

data class BottomNavigationItem(
    val label: String = Screens.Home.title,
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = Screens.Home.route
) {

    @Composable
    fun bottomNavigationItems(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = Screens.Home.title,
                icon = Screens.Home.icon,
                route = Screens.Home.route
            ),
            BottomNavigationItem(
                label = Screens.Records.title,
                icon = Screens.Records.icon,
                route = Screens.Records.route
            ),
        )
    }
}