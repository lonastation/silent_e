package com.linn.silent_e.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.linn.silent_e.ui.screens.HomeScreen
import com.linn.silent_e.ui.screens.RecordScreen

@Composable
fun SilentNavHost(
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