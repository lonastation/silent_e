package com.linn.silent_e.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screens("home", "Home", Icons.Default.Home)
    data object Records : Screens("records", "Record", Icons.Default.Audiotrack)
} 