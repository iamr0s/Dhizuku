package com.rosan.dhizuku.ui.page.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rosan.dhizuku.ui.page.settings.main.MainPage

@Composable
fun SettingsPage(windowInsets: WindowInsets) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsScreen.Main.route,
    ) {
        composable(route = SettingsScreen.Main.route) {
            MainPage(
                windowInsets = windowInsets,
                navController = navController
            )
        }
    }
}
