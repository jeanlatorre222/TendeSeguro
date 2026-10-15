package com.example.tendeseguro20

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme

// ============================================================================
// ELEMENTOS DE NAVEGACIÓN INFERIOR (BottomNavItem)
// ============================================================================

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val contentDescription: String,
) {
    data object Dashboard : BottomNavItem(
        route = "dashboard_tab",
        title = "Monitoreo",
        icon = Icons.Default.Home,
        contentDescription = "Pestaña de monitoreo en tiempo real de temperatura, humedad y agua",
    )

    data object Alerts : BottomNavItem(
        route = "alerts_tab",
        title = "Alertas",
        icon = Icons.Default.Notifications,
        contentDescription = "Pestaña de centro de historial de alertas IoT y notificaciones FCM",
    )

    data object Settings : BottomNavItem(
        route = "settings_tab",
        title = "Ajustes",
        icon = Icons.Default.Person,
        contentDescription = "Pestaña de perfil de usuario, alertas y preferencias de notificaciones",
    )
}

// ============================================================================
// CONTENEDOR ANFITRIÓN CON RUTAS DE ID DE SENSOR ÚNICO
// ============================================================================

/**
 * Contenedor anfitrión de la navegación principal persistente.
 * Transfiere únicamente el `sensorId` hacia las Vistas de Profundidad (Detalle/Formulario)
 * garantizando sincronización total en tiempo real con el IotSensorRepository.
 */
@Composable
fun MainScreenHost(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Alerts,
        BottomNavItem.Settings,
    )

    val alertHistoryViewModel: AlertHistoryViewModel = viewModel()

    val isBottomBarVisible = currentRoute == BottomNavItem.Dashboard.route ||
            currentRoute == BottomNavItem.Alerts.route ||
            currentRoute == BottomNavItem.Settings.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = PastelDesignSystem.Background,
                    contentColor = PastelDesignSystem.TextPrimary,
                    tonalElevation = 8.dp,
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription,
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PastelDesignSystem.TempAccent,
                                selectedTextColor = PastelDesignSystem.TempAccent,
                                unselectedIconColor = PastelDesignSystem.TextSecondary,
                                unselectedTextColor = PastelDesignSystem.TextSecondary,
                                indicatorColor = PastelDesignSystem.TempBackground,
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = "${item.title}. ${if (isSelected) "Pestaña seleccionada" else "Presiona para navegar a esta sección"}"
                            },
                        )
                    }
                }
            }
        },
        containerColor = PastelDesignSystem.Background,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    onSettingsClick = {
                        navController.navigate(BottomNavItem.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSensorDetailClick = { sensorType ->
                        // PASO ÚNICO DEL SENSOR ID POR NAVEGACIÓN
                        val sensorId = when (sensorType) {
                            SensorType.TEMPERATURE -> "sensor_temp"
                            SensorType.HUMIDITY -> "sensor_humid"
                            SensorType.WATER_BX5 -> "sensor_water"
                        }
                        navController.navigate("sensor_detail/$sensorId")
                    },
                )
            }

            composable(BottomNavItem.Alerts.route) {
                AlertHistoryScreen(
                    viewModel = alertHistoryViewModel,
                    onNavigateBack = {
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogoutClick = onLogoutClick,
                )
            }

            // RUTA TIPADA CON PASO ÚNICO DEL SENSOR ID
            composable(
                route = "sensor_detail/{sensorId}",
                arguments = listOf(
                    navArgument("sensorId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val sensorDetailViewModel: SensorDetailViewModel = viewModel(backStackEntry)
                SensorDetailScreen(
                    viewModel = sensorDetailViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenHostPreview() {
    TendeSeguro20Theme {
        MainScreenHost(onLogoutClick = {})
    }
}
