package com.example.tendeseguro20

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme
import kotlinx.coroutines.launch

// ============================================================================
// COLORES PASTEL PARA ONBOARDING
// ============================================================================

private object OnboardingPastel {
    val Background = Color(0xFFFBF9F5)
    val PrimaryText = Color(0xFF2C3E50)
    val SecondaryText = Color(0xFF7F8C8D)
    val AccentBlue = Color(0xFF4A90E2)
    val AccentGreen = Color(0xFF7BCD9B)
    val AccentOrange = Color(0xFFE67E22)
    val CardBackgroundPage1 = Color(0xFFEBF3FE)
    val CardBackgroundPage2 = Color(0xFFEBF8F2)
    val CardBackgroundPage3 = Color(0xFFFFF3EC)
    val BorderPage1 = Color(0xFFD0E2FF)
    val BorderPage2 = Color(0xFFC7EFE0)
    val BorderPage3 = Color(0xFFFFE0D1)
}

// ============================================================================
// MODELO DE DATOS DE PÁGINAS DE ONBOARDING
// ============================================================================

data class OnboardingPageData(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val isPermissionPage: Boolean = false,
)

// ============================================================================
// COMPOSABLE PRINCIPAL: OnboardingScreen
// ============================================================================

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Estado para registrar si los permisos han sido otorgados
    var permissionsGranted by remember { mutableStateOf(false) }

    // Launcher para solicitud múltiple de permisos críticos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { resultMap ->
        permissionsGranted = resultMap.values.all { it }
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
            title = "Detección BX5 y Notificaciones",
            description = "Para avisarte en tiempo real de alertas de lluvia y enlazar tus módulos de agua BX5, solicitamos permisos de notificaciones y ubicación.",
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
            // Header: Botón Saltar
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

            // Contenido Deslizable (Pager)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                OnboardingPageView(
                    page = pages[pageIndex],
                    permissionsGranted = permissionsGranted,
                    onRequestPermissions = {
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                )
            }

            // Footer: Indicadores de página y Botones de navegación
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Indicador de Puntos (Page Indicator)
                PagerIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                )

                // Botones Siguiente / Empezar
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
                            shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
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
}

// ============================================================================
// VISTA INDIVIDUAL DE CADA PÁGINA
// ============================================================================

@Composable
private fun OnboardingPageView(
    page: OnboardingPageData,
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
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
                .clip(RoundedCornerShape(28.dp))
                .border(1.5.dp, page.borderColor, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = page.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Emoji / Icono destacado
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

                // Panel contextual de solicitud de permisos (Sólamente en la vista 3)
                if (page.isPermissionPage) {
                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(visible = !permissionsGranted) {
                        Button(
                            onClick = onRequestPermissions,
                            shape = RoundedCornerShape(16.dp),
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
                                text = "Conceder Permisos",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    AnimatedVisibility(visible = permissionsGranted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
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
                                text = "Permisos concedidos correctamente",
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

// ============================================================================
// INDICADOR DE PÁGINAS (PAGER INDICATOR)
// ============================================================================

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

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    TendeSeguro20Theme {
        OnboardingScreen(onOnboardingFinished = {})
    }
}
