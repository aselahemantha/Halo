package com.example.halo

import android.content.Intent
import android.os.Bundle
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
import com.example.halo.ui.viewmodel.MainViewModel
import com.example.halo.ui.navigation.HaloNavHost
import com.example.halo.ui.navigation.Screen
import com.example.halo.ui.theme.HaloTheme
import com.example.halo.data.repository.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.google.android.gms.maps.MapsInitializer.initialize(applicationContext, com.google.android.gms.maps.MapsInitializer.Renderer.LATEST, null)

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
                        if (networkStatus == com.example.halo.data.network.ConnectivityObserver.Status.Lost ||
                            networkStatus == com.example.halo.data.network.ConnectivityObserver.Status.Unavailable ||
                            networkStatus == com.example.halo.data.network.ConnectivityObserver.Status.Losing
                        ) {
                            com.example.halo.ui.components.OfflineBanner(
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
        if (intent?.getStringExtra("navigate_to") == "trigger_screen") {
            val alarmId = intent.getStringExtra("alarm_id") ?: return
            navController.navigate(Screen.AlarmTrigger.createRoute(alarmId)) {
                launchSingleTop = true
            }
        }
    }
}