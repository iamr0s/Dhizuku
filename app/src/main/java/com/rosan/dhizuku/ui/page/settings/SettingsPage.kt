package com.rosan.dhizuku.ui.page.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.rosan.dhizuku.ui.page.settings.activate.ActivatePage
import com.rosan.dhizuku.ui.page.settings.app_management.AppManagementPage
import com.rosan.dhizuku.ui.page.settings.home.HomePage

@Composable
fun SettingsPage(windowInsets: WindowInsets) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsRoute.Home.route,
    ) {
        composable(route = SettingsRoute.Home.route) {
            HomePage(
                windowInsets = windowInsets,
                navController = navController
            )
        }
        composable(route = SettingsRoute.AppManagement.route) {
            AppManagementPage(
                windowInsets = windowInsets,
                navController = navController
            )
        }
        composable(route = SettingsRoute.Activate.route) {
            val mode = it.arguments?.getString("mode")?.let { name ->
                SettingsRoute.Activate.Mode.valueOf(name)
            } ?: SettingsRoute.Activate.Mode.Dhizuku

            ActivatePage(
                windowInsets = windowInsets,
                navController = navController,
                mode = mode
            )
        }
    }
}