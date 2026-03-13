package com.exoticstech.halo.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddAlarm : Screen("add_alarm")
    object AlarmTrigger : Screen("alarm_trigger/{alarmId}") {
        fun createRoute(alarmId: String) = "alarm_trigger/$alarmId"
    }
    object EditAlarm : Screen("edit_alarm/{alarmId}") {
        fun createRoute(alarmId: Long) = "edit_alarm/$alarmId"
    }
    object AlarmHistory : Screen("alarm_history")
    object Settings : Screen("settings")
    object Walkthrough : Screen("walkthrough")
    object Splash : Screen("splash")
}
