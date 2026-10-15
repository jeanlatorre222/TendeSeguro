package com.example.tendeseguro20

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Punto de entrada principal de la aplicación.
 * Integra la SplashScreen API oficial (Android 12+), AppRouter y la conmutación global del Tema Claro/Oscuro.
 */
class MainActivity : FragmentActivity() {

    private var initialDestination by mutableStateOf<AppDestination>(AppDestination.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Splash Screen Nativa (Android 12+)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Mantiene Splash Screen únicamente mientras resuelve el destino inicial en segundo plano
        splashScreen.setKeepOnScreenCondition {
            initialDestination == AppDestination.Loading
        }

        // 3. Enrutamiento silencioso asíncrono
        resolveAppRouting()

        enableEdgeToEdge()

        setContent {
            // CONEXIÓN DIRECTA Y REACTIVA CON AppThemeState.isDarkModeEnabled
            TendeSeguro20Theme(darkTheme = AppThemeState.isDarkModeEnabled) {
                val onboardingPreferences = remember { OnboardingPreferences(applicationContext) }
                val secureTokenManager = remember { SecureTokenManager(applicationContext) }

                if (initialDestination != AppDestination.Loading) {
                    AppNavigation(
                        initialDestination = initialDestination,
                        onboardingPreferences = onboardingPreferences,
                        secureTokenManager = secureTokenManager,
                    )
                }
            }
        }
    }

    private fun resolveAppRouting() {
        lifecycleScope.launch(Dispatchers.IO) {
            val appRouter = AppRouter(applicationContext)
            val resolvedDestination = appRouter.resolveInitialDestination()

            withContext(Dispatchers.Main) {
                initialDestination = resolvedDestination
            }
        }
    }
}
