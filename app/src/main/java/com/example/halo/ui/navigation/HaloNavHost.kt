package com.example.halo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.halo.ui.screens.add_location_alarm.AddLocationAlarmScreen
import com.example.halo.ui.screens.alarm_trigger.AlarmTriggerScreen
import com.example.halo.ui.screens.home.HomeScreen
import com.example.halo.ui.screens.settings.SettingsScreen
import com.example.halo.ui.screens.walkthrough.WalkthroughScreen
import com.example.halo.ui.screens.splash.SplashScreen

@Composable
fun HaloNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Walkthrough.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddAlarm = { navController.navigate(Screen.AddAlarm.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.AddAlarm.route) {
            AddLocationAlarmScreen(
                onNavigateBack = { navController.popBackStack() },
                onAlarmSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AlarmTrigger.route,
            arguments = listOf(navArgument("alarmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId") ?: ""
            AlarmTriggerScreen(
                alarmId = alarmId,
                onNavigateBack = {
                    // Navigate only to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWalkthrough = { navController.navigate(Screen.Walkthrough.route) }
            )
        }
        composable(Screen.Walkthrough.route) {
            WalkthroughScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Walkthrough.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
