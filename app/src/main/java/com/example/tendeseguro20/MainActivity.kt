package com.example.tendeseguro20

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {

    private var isAppReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isAppReady }

        initializeIotResources()

        enableEdgeToEdge()

        setContent {
            TendeSeguro20Theme {
                val onboardingPreferences = remember { OnboardingPreferences(applicationContext) }
                val secureTokenManager = remember { SecureTokenManager(applicationContext) }

                val isOnboardingCompleted by onboardingPreferences.isOnboardingCompleted.collectAsState(initial = null)
                var isAuthenticated by remember { mutableStateOf(secureTokenManager.getAuthToken() != null) }
                var currentTab by remember { mutableStateOf("dashboard") }

                val coroutineScope = rememberCoroutineScope()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        isOnboardingCompleted == false -> OnboardingScreen(
                            onOnboardingFinished = {
                                coroutineScope.launch {
                                    onboardingPreferences.setOnboardingCompleted(true)
                                }
                            },
                            modifier = Modifier.padding(innerPadding),
                        )

                        !isAuthenticated -> AuthScreen(
                            onAuthSuccess = {
                                isAuthenticated = true
                                currentTab = "dashboard"
                            },
                            modifier = Modifier.padding(innerPadding),
                        )

                        else -> Crossfade(targetState = currentTab, label = "MainNavigation") { screen ->
                            when (screen) {
                                "dashboard" -> DashboardScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onSettingsClick = { currentTab = "settings" },
                                )

                                "settings" -> SettingsScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onNavigateBack = { currentTab = "dashboard" },
                                    onLogoutClick = {
                                        secureTokenManager.clearSession()
                                        isAuthenticated = false
                                        currentTab = "dashboard"
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initializeIotResources() {
        lifecycleScope.launch(Dispatchers.IO) {
            loadInitialSensorData()

            withContext(Dispatchers.Main) {
                isAppReady = true
            }
        }
    }

    private fun loadInitialSensorData() {
        // Carga inicial de sensores / configuración IoT
    }
}
