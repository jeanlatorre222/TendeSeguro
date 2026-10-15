package com.example.tendeseguro20

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ============================================================================
// MODELO DE DESTINOS DE NAVEGACIÓN (AppDestination)
// ============================================================================

/**
 * Representa los destinos principales de la aplicación resueltos por el Router Lógico.
 */
sealed interface AppDestination {
    data object Loading : AppDestination
    data object Onboarding : AppDestination
    data object Auth : AppDestination
    data object AppTour : AppDestination
    data object Dashboard : AppDestination
}

// ============================================================================
// MANEJADOR LÓGICO DE ENRUTAMIENTO (AppRouter)
// ============================================================================

/**
 * Controlador de enrutamiento que decide de forma silenciosa e ininterrumpida
 * el destino inicial del usuario analizando las preferencias y la sesión en segundo plano.
 */
class AppRouter(context: Context) {

    private val onboardingPreferences = OnboardingPreferences(context)
    private val secureTokenManager = SecureTokenManager(context)

    /**
     * Evalúa de forma asíncrona la condición del usuario:
     * - Si es la primera vez -> Onboarding
     * - Si no completó o expiró la sesión -> Auth (Login/Registro)
     * - Si no ha visto el App Tour post-login -> AppTour
     * - Si tiene token seguro válido y tour completado -> Dashboard
     */
    suspend fun resolveInitialDestination(): AppDestination {
        val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.first()
        val hasValidToken = secureTokenManager.getAuthToken() != null
        val hasCompletedAppTour = onboardingPreferences.hasCompletedAppTour.first()

        return when {
            !isOnboardingCompleted -> AppDestination.Onboarding
            !hasValidToken -> AppDestination.Auth
            !hasCompletedAppTour -> AppDestination.AppTour
            else -> AppDestination.Dashboard
        }
    }
}

// ============================================================================
// NAVHOST / CONTENEDOR REACTIVO DE NAVEGACIÓN (AppNavigation)
// ============================================================================

/**
 * Componente principal de navegación en Jetpack Compose que conmuta suavemente
 * entre las vistas según el estado resuelto por el AppRouter.
 */
@Composable
fun AppNavigation(
    initialDestination: AppDestination,
    onboardingPreferences: OnboardingPreferences,
    secureTokenManager: SecureTokenManager,
    modifier: Modifier = Modifier,
) {
    var currentDestination by remember { mutableStateOf(initialDestination) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            animationSpec = tween(durationMillis = 400),
            label = "AppNavigationCrossfade",
        ) { destination ->
            when (destination) {
                is AppDestination.Loading -> {
                    // Contenido neutro mientras resuelve la inicialización
                }

                is AppDestination.Onboarding -> OnboardingScreen(
                    onOnboardingFinished = {
                        coroutineScope.launch {
                            onboardingPreferences.setOnboardingCompleted(true)
                            currentDestination = AppDestination.Auth
                        }
                    },
                    modifier = Modifier.padding(innerPadding),
                )

                is AppDestination.Auth -> AuthScreen(
                    onAuthSuccess = {
                        coroutineScope.launch {
                            val tourCompleted = onboardingPreferences.hasCompletedAppTour.first()
                            currentDestination = if (!tourCompleted) {
                                AppDestination.AppTour
                            } else {
                                AppDestination.Dashboard
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding),
                )

                is AppDestination.AppTour -> AppTourScreen(
                    onTourFinished = {
                        coroutineScope.launch {
                            onboardingPreferences.setAppTourCompleted(true)
                            currentDestination = AppDestination.Dashboard
                        }
                    },
                    modifier = Modifier.padding(innerPadding),
                )

                is AppDestination.Dashboard -> MainScreenHost(
                    onLogoutClick = {
                        secureTokenManager.clearSession()
                        currentDestination = AppDestination.Auth
                    },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
