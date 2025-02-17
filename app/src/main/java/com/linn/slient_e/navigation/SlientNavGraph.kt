package com.linn.slient_e.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.linn.slient_e.home.HomeScreen
import com.linn.slient_e.record.RecordScreen

@Composable
fun SlientNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route,
        modifier = modifier
    ) {
        composable(Screens.Home.route) {
            HomeScreen()
        }
        composable(Screens.Records.route) {
            RecordScreen()
        }
    }
}