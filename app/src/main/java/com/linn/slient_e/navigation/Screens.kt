package com.linn.slient_e.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screens("home", "Home", Icons.Default.Home)
    object Records : Screens("records", "Records", Icons.Default.Audiotrack)

    companion object {
        val screens = listOf(Home, Records)
    }
} 