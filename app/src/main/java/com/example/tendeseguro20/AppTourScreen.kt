package com.example.tendeseguro20

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
import androidx.compose.runtime.rememberCoroutineScope
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
// MODELO DE DATOS DE TARJETAS DEL TOUR
// ============================================================================

data class AppTourSlideData(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val badgeLabel: String,
    val backgroundColor: Color,
    val borderColor: Color,
)

// ============================================================================
// COMPOSABLE PRINCIPAL: AppTourScreen (RECORRIDO INICIAL POST-LOGIN)
// ============================================================================

/**
 * Pantalla de Recorrido Inicial (App Tour / Feature Discovery) que presenta
 * de forma visual e interactiva las capacidades clave de TendeSeguro 2.0.
 */
@Composable
fun AppTourScreen(
    onTourFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val tourSlides = listOf(
        AppTourSlideData(
            title = "Monitoreo en Tiempo Real",
            description = "Consulta constantemente lecturas térmicas y niveles de humedad ambiental recopilados por tus sensores inalámbricos ESP32.",
            iconEmoji = "🌡️ 💧",
            badgeLabel = "Módulo Térmico",
            backgroundColor = PastelDesignSystem.TempBackground,
            borderColor = PastelDesignSystem.TempBorder,
        ),
        AppTourSlideData(
            title = "Módulo de Agua BX5",
            description = "Detecta acumulaciones excesivas de agua o precipitaciones líquidas en la plataforma para prevenir daños en tu ropa.",
            iconEmoji = "🌊 📊",
            badgeLabel = "Sensor BX5",
            backgroundColor = PastelDesignSystem.WaterBackground,
            borderColor = PastelDesignSystem.WaterBorder,
        ),
        AppTourSlideData(
            title = "Semáforo Preventivo de Lluvia",
            description = "Visualiza el semáforo inteligente de saturación que te advierte con alertas de color cuando la humedad supera el 80%.",
            iconEmoji = "🚨 ☂️",
            badgeLabel = "Protección Activa",
            backgroundColor = PastelDesignSystem.HeroWarningBackground,
            borderColor = PastelDesignSystem.HeroWarningBorder,
        ),
        AppTourSlideData(
            title = "Ajustes y Umbrales a la Medida",
            description = "Personaliza tus umbrales de alerta de temperatura y humedad, gestiona notificaciones push y protege tu sesión con biometría.",
            iconEmoji = "⚙️ 🔐",
            badgeLabel = "Control Total",
            backgroundColor = PastelDesignSystem.HumidBackground,
            borderColor = PastelDesignSystem.HumidBorder,
        ),
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PastelDesignSystem.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header: Título y Botón Saltar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "TendeSeguro 2.0",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelDesignSystem.TextPrimary,
                    )
                    Text(
                        text = "Descubre las funciones clave",
                        fontSize = 12.sp,
                        color = PastelDesignSystem.TextSecondary,
                    )
                }

                if (pagerState.currentPage < tourSlides.size - 1) {
                    TextButton(onClick = onTourFinished) {
                        Text(
                            text = "Saltar Tour",
                            color = PastelDesignSystem.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Paginador Horizontal
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                AppTourSlideView(slide = tourSlides[pageIndex])
            }

            // Footer: Puntos Indicadores y Botones
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Indicador de Puntos (Dots)
                PagerIndicator(
                    pageCount = tourSlides.size,
                    currentPage = pagerState.currentPage,
                )

                // Botones de Navegación del Tour
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
                                contentColor = PastelDesignSystem.TextPrimary,
                            ),
                        ) {
                            Text(text = "Anterior", fontSize = 14.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < tourSlides.size - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onTourFinished()
                            }
                        },
                        shape = PastelDesignSystem.ShapeButton,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pagerState.currentPage == tourSlides.size - 1)
                                PastelDesignSystem.TrafficGreen
                            else
                                PastelDesignSystem.TempAccent,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    ) {
                        Text(
                            text = if (pagerState.currentPage == tourSlides.size - 1) "Comenzar" else "Siguiente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (pagerState.currentPage == tourSlides.size - 1)
                                Icons.Default.CheckCircle
                            else
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
// VISTA INDIVIDUAL DE CADA SLIDE
// ============================================================================

@Composable
private fun AppTourSlideView(slide: AppTourSlideData) {
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
                .border(1.5.dp, slide.borderColor, PastelDesignSystem.ShapeHeroCard),
            shape = PastelDesignSystem.ShapeHeroCard,
            colors = CardDefaults.cardColors(containerColor = slide.backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Insignia Superior
                Box(
                    modifier = Modifier
                        .clip(PastelDesignSystem.ShapeBadge)
                        .background(slide.borderColor.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = slide.badgeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelDesignSystem.TextPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Icono / Emoji Destacado
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(PastelDesignSystem.ShapeCircle)
                        .background(slide.borderColor.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = slide.iconEmoji,
                        fontSize = 38.sp,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = slide.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelDesignSystem.TextPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = slide.description,
                    fontSize = 14.sp,
                    color = PastelDesignSystem.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

// ============================================================================
// INDICADOR DE PÁGINAS (PAGED DOTS)
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
                    .clip(PastelDesignSystem.ShapeCircle)
                    .background(
                        if (isSelected) PastelDesignSystem.TempAccent else PastelDesignSystem.TextSecondary.copy(alpha = 0.3f),
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
fun AppTourScreenPreview() {
    TendeSeguro20Theme {
        AppTourScreen(onTourFinished = {})
    }
}
