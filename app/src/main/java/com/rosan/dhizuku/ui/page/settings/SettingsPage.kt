package com.rosan.dhizuku.ui.page.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.rosan.dhizuku.ui.page.settings.main.MainPage

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsPage() {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = SettingsScreen.Main.route,
    ) {
        composable(
            route = SettingsScreen.Main.route,
            enterTransition = {
                null
            },
            exitTransition = {
                null
            },
            popEnterTransition = {
                null
            },
            popExitTransition = {
                null
            }
        ) {
            MainPage(navController = navController)
        }
    }
}
