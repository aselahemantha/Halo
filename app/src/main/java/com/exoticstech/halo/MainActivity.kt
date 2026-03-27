package com.exoticstech.halo

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exoticstech.halo.ui.viewmodel.MainViewModel
import com.exoticstech.halo.ui.navigation.HaloNavHost
import com.exoticstech.halo.ui.navigation.Screen
import com.exoticstech.halo.ui.theme.HaloTheme
import com.exoticstech.halo.data.repository.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.google.android.gms.maps.MapsInitializer.initialize(applicationContext, com.google.android.gms.maps.MapsInitializer.Renderer.LATEST, null)

        showOnLockScreen()

        setContent {
            val viewModel = hiltViewModel<MainViewModel>()
            val appTheme by viewModel.appTheme.collectAsState()
            
            // Use system default if null
            val darkTheme = when (appTheme) {
                 AppTheme.LIGHT -> false
                 AppTheme.DARK -> true
                 AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            HaloTheme(darkTheme = darkTheme) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val networkStatus by viewModel.networkStatus.collectAsState()
                    

                    
                    LaunchedEffect(intent) {
                        handleIntent(intent, navController)
                    }
                    
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        HaloNavHost(navController = navController)

                        // Network Banner
                        if (networkStatus == com.exoticstech.halo.data.network.ConnectivityObserver.Status.Lost ||
                            networkStatus == com.exoticstech.halo.data.network.ConnectivityObserver.Status.Unavailable ||
                            networkStatus == com.exoticstech.halo.data.network.ConnectivityObserver.Status.Losing
                        ) {
                            com.exoticstech.halo.ui.components.OfflineBanner(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(16.dp),
                                onRetry = { 
                                    // Optional: Trigger a manual check or simply let the user know they can retry
                                    // For now, checks are automatic.
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // key for handleIntent to see new intent in LaunchedEffect if recomposed, 
                          // but actually LaunchedEffect(intent) might not trigger if intent object instance is same?
                          // safer to rely on state or re-trigger. 
                          // For simplicity in Compose, re-setting intent allows us to check it.
                          // Ideally we use a side-effect that listens.
    }

    private fun handleIntent(intent: Intent?, navController: NavHostController) {
        if (intent == null) return

        // Handle alarm trigger deep link
        if (intent.getStringExtra("navigate_to") == "trigger_screen") {
            val alarmId = intent.getStringExtra("alarm_id") ?: return
            navController.navigate(Screen.AlarmTrigger.createRoute(alarmId)) {
                launchSingleTop = true
            }
            return
        }

        // Handle shared alarm URI deep link (halo://alarm?...)
        val data = intent.data

        if (data != null && data.scheme == "halo" && data.host == "alarm_shortcut") {
            navController.navigate(Screen.AddAlarm.route) {
                launchSingleTop = true
            }
            intent.data = null
            return
        }

        if (data != null && data.scheme == "halo" && data.host == "alarm") {
            val name = data.getQueryParameter("name") ?: ""
            val lat = data.getQueryParameter("lat")?.toDoubleOrNull() ?: 0.0
            val lng = data.getQueryParameter("lng")?.toDoubleOrNull() ?: 0.0
            val radius = data.getQueryParameter("radius")?.toDoubleOrNull() ?: 800.0
            val category = data.getQueryParameter("category") ?: "General"

            // Since our nav graph doesn't pass all these as primitives in the route by default,
            // we can use the NavController's SavedStateHandle to pass complex data or multiple fields to AddAlarmScreen / AddAlarmViewModel.
            // A common pattern is to set it in the currentBackStackEntry before navigating:
            navController.currentBackStackEntry?.savedStateHandle?.set("shared_name", name)
            navController.currentBackStackEntry?.savedStateHandle?.set("shared_lat", lat)
            navController.currentBackStackEntry?.savedStateHandle?.set("shared_lng", lng)
            navController.currentBackStackEntry?.savedStateHandle?.set("shared_radius", radius)
            navController.currentBackStackEntry?.savedStateHandle?.set("shared_category", category)

            navController.navigate(Screen.AddAlarm.route) {
                launchSingleTop = true
            }
            // Clear the intent data so it doesn't trigger again on rotation
            intent.data = null
        }
    }

    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }
}