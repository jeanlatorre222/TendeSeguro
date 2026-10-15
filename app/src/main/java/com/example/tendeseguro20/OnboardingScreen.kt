package com.example.tendeseguro20

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme
import kotlinx.coroutines.launch

// ============================================================================
// COLORES PASTEL PARA ONBOARDING
// ============================================================================

private object OnboardingPastel {
    val Background = Color(0xFFFBF9F5)
    val PrimaryText = Color(0xFF1E293B)
    val SecondaryText = Color(0xFF475569)
    val AccentBlue = Color(0xFF1D4ED8)
    val AccentGreen = Color(0xFF15803D)
    val AccentOrange = Color(0xFFC2410C)
    val CardBackgroundPage1 = Color(0xFFEBF3FE)
    val CardBackgroundPage2 = Color(0xFFEBF8F2)
    val CardBackgroundPage3 = Color(0xFFFFF3EC)
    val BorderPage1 = Color(0xFFD0E2FF)
    val BorderPage2 = Color(0xFFC7EFE0)
    val BorderPage3 = Color(0xFFFFE0D1)
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val isPermissionPage: Boolean = false,
)

// ============================================================================
// COMPOSABLE PRINCIPAL CON SOLICITUD MÚLTIPLE DE PERMISOS REALES
// ============================================================================

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Consulta en tiempo real de los permisos de Notificaciones y Ubicación
    var arePermissionsGranted by remember {
        mutableStateOf(
            checkRealPermissions(context),
        )
    }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsRedirectDialog by remember { mutableStateOf(false) }

    // LANZADOR REAL MÚLTIPLE DE PERMISOS CON ACTIVITY RESULT CONTRACT
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsResultMap ->
        val allGranted = permissionsResultMap.values.all { it }
        arePermissionsGranted = allGranted
        if (!allGranted) {
            showRationaleDialog = true
        }
    }

    val pages = listOf(
        OnboardingPageData(
            title = "Monitoreo Térmico y Humedad",
            description = "Supervisa la temperatura ambiental y la humedad relativa en tiempo real con datos precisos obtenidos de tus sensores IoT.",
            iconEmoji = "🌡️ 💧",
            backgroundColor = OnboardingPastel.CardBackgroundPage1,
            borderColor = OnboardingPastel.BorderPage1,
        ),
        OnboardingPageData(
            title = "Prevención Inteligente de Lluvia",
            description = "Recibe alertas preventivas antes de que empiece a llover gracias al sistema de semáforo de saturación para proteger tu ropa.",
            iconEmoji = "🌧️ 🚦",
            backgroundColor = OnboardingPastel.CardBackgroundPage2,
            borderColor = OnboardingPastel.BorderPage2,
        ),
        OnboardingPageData(
            title = "Permisos de Notificación y Ubicación",
            description = "Para enviarte alertas inmediatas ante lluvia inminente y localizar estaciones meteorológicas locales, requerimos permisos de notificaciones y ubicación.",
            iconEmoji = "🌊 🔔",
            backgroundColor = OnboardingPastel.CardBackgroundPage3,
            borderColor = OnboardingPastel.BorderPage3,
            isPermissionPage = true,
        ),
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = OnboardingPastel.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onOnboardingFinished) {
                        Text(
                            text = "Saltar",
                            color = OnboardingPastel.SecondaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                OnboardingPageView(
                    page = pages[pageIndex],
                    isPermissionGranted = arePermissionsGranted,
                    onRequestPermission = {
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        // Lanza el diálogo nativo del sistema operativo Android
                        multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                    onOpenSettings = {
                        showSettingsRedirectDialog = true
                    },
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                PagerIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            shape = PastelDesignSystem.ShapeButton,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnboardingPastel.PrimaryText,
                            ),
                        ) {
                            Text(text = "Anterior", fontSize = 14.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onOnboardingFinished()
                            }
                        },
                        shape = PastelDesignSystem.ShapeButton,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnboardingPastel.AccentBlue,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) "Comenzar" else "Siguiente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO EXPLICATIVO DE PERMISOS (RATIONALE DIALOG)
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = {
                Text(
                    text = "Permisos Necesarios para TendeSeguro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingPastel.PrimaryText,
                )
            },
            text = {
                Text(
                    text = "Los permisos de notificaciones y ubicación son indispensables para avisarte al instante cuando la humedad alcance el nivel de lluvia y consultar datos del clima local.",
                    fontSize = 13.sp,
                    color = OnboardingPastel.SecondaryText,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnboardingPastel.AccentBlue),
                ) {
                    Text("Reintentar Solicitud", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRationaleDialog = false }) {
                    Text("Continuar sin Permisos")
                }
            },
            shape = PastelDesignSystem.ShapeSensorCard,
            containerColor = OnboardingPastel.Background,
        )
    }

    // DIÁLOGO DE REDIRECCIÓN A AJUSTES DE ANDROID
    if (showSettingsRedirectDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsRedirectDialog = false },
            title = {
                Text(
                    text = "Habilitar Permisos en Ajustes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingPastel.PrimaryText,
                )
            },
            text = {
                Text(
                    text = "Si rechazaste los permisos con la opción 'No volver a preguntar', por favor actívalos manualmente en los ajustes del sistema.",
                    fontSize = 13.sp,
                    color = OnboardingPastel.SecondaryText,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsRedirectDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OnboardingPastel.AccentOrange),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Ajustes de Android", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSettingsRedirectDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = PastelDesignSystem.ShapeSensorCard,
            containerColor = OnboardingPastel.Background,
        )
    }
}

private fun checkRealPermissions(context: android.content.Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    return fineLocation && notifications
}

@Composable
private fun OnboardingPageView(
    page: OnboardingPageData,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PastelDesignSystem.ShapeHeroCard)
                .border(1.5.dp, page.borderColor, PastelDesignSystem.ShapeHeroCard),
            shape = PastelDesignSystem.ShapeHeroCard,
            colors = CardDefaults.cardColors(containerColor = page.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(page.borderColor.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = page.iconEmoji,
                        fontSize = 38.sp,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = page.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardingPastel.PrimaryText,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = page.description,
                    fontSize = 14.sp,
                    color = OnboardingPastel.SecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                if (page.isPermissionPage) {
                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(visible = !isPermissionGranted) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = onRequestPermission,
                                shape = PastelDesignSystem.ShapeButton,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnboardingPastel.AccentOrange,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Conceder Permisos Nativos",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            TextButton(onClick = onOpenSettings) {
                                Text(
                                    text = "Configurar en Ajustes del Sistema",
                                    fontSize = 11.sp,
                                    color = OnboardingPastel.SecondaryText,
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isPermissionGranted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(PastelDesignSystem.ShapeBadge)
                                .background(OnboardingPastel.AccentGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = OnboardingPastel.AccentGreen,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Permisos de notificaciones y clima concedidos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnboardingPastel.PrimaryText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .size(
                        width = if (isSelected) 24.dp else 8.dp,
                        height = 8.dp,
                    )
                    .clip(CircleShape)
                    .background(
                        if (isSelected) OnboardingPastel.AccentBlue else OnboardingPastel.SecondaryText.copy(alpha = 0.3f),
                    ),
            )
        }
    }
}
