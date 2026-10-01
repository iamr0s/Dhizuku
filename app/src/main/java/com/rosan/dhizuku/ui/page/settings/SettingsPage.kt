package com.rosan.dhizuku.ui.page.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

import com.rosan.dhizuku.ui.page.settings.activate.ActivatePage
import com.rosan.dhizuku.ui.page.settings.account_manager.AccountManagerPage
import com.rosan.dhizuku.ui.page.settings.app_management.AppManagementPage
import com.rosan.dhizuku.ui.page.settings.home.HomePage
import com.rosan.dhizuku.ui.page.settings.settings.SettingsPage
import com.rosan.dhizuku.ui.page.settings.user_manager.UserManagerPage

@Composable
fun SettingsPage(windowInsets: WindowInsets) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsRoute.Home.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
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
        composable(route = SettingsRoute.UserManagement.route) {
            UserManagerPage(
                windowInsets = windowInsets,
                onNavigateToAccount = { userId ->
                    navController.navigate(SettingsRoute.AccountManagement.route(userId))
                },
                onBack = navController::navigateUp
            )
        }
        composable(
            route = SettingsRoute.AccountManagement.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                }
            )
        ) {
            val userId = it.arguments?.getInt("id")
            if (userId == null) {
                navController.navigateUp()
                return@composable
            }
            AccountManagerPage(
                windowInsets = windowInsets,
                userId = userId,
                onBack = navController::navigateUp
            )
        }
        composable(route = SettingsRoute.Settings.route) {
            SettingsPage(
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
