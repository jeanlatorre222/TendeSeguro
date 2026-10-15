package com.example.tendeseguro20

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ============================================================================
// SISTEMA DE DISEÑO CON PALETA OSCURA PROFESIONAL (#121212 / #1E1E1E) Y CLARA
// ============================================================================

object PastelDesignSystem {

    // ------------------------------------------------------------------------
    // 1. PALETA DE COLORES DINÁMICA (CLARA / OSCURA PROFESIONAL M3)
    // ------------------------------------------------------------------------

    val Background: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF121212) else Color(0xFFFAF8F5)

    val TextPrimary: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFFF8FAFC) else Color(0xFF1E293B)

    val TextSecondary: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF94A3B8) else Color(0xFF475569)

    val CardSurface: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)

    // Módulo Temperatura (Azul)
    val TempBackground: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF1E293B) else Color(0xFFEBF3FE)

    val TempBorder: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF334155) else Color(0xFFD0E2FF)

    val TempAccent: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF60A5FA) else Color(0xFF1D4ED8)

    // Módulo Humedad (Verde Menta)
    val HumidBackground: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF143826) else Color(0xFFEBF8F2)

    val HumidBorder: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF1D523B) else Color(0xFFC7EFE0)

    val HumidAccent: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF4ADE80) else Color(0xFF15803D)

    // Módulo Agua BX5 (Melocotón / Naranja)
    val WaterBackground: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF362115) else Color(0xFFFFF3EC)

    val WaterBorder: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF5E331A) else Color(0xFFFFE0D1)

    val WaterAccent: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFFFB923C) else Color(0xFFC2410C)

    // Tarjeta de Acción Hero
    val HeroSafeBackground: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF112E21) else Color(0xFFEAFAF1)

    val HeroSafeBorder: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF1D523B) else Color(0xFFA2E8DD)

    val HeroWarningBackground: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF3B1719) else Color(0xFFFFF0ED)

    val HeroWarningBorder: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF692429) else Color(0xFFFFC4B8)

    // Alertas y Advertencias
    val AlertBackground: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF3B1719) else Color(0xFFFFF0ED)

    val AlertBorder: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF692429) else Color(0xFFFFC4B8)

    val AlertRed: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFFF87171) else Color(0xFFB91C1C)

    // Semáforo Preventivo
    val TrafficGreen: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFF4ADE80) else Color(0xFF15803D)

    val TrafficYellow: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFFFBBF24) else Color(0xFFB45309)

    val TrafficRed: Color
        @Composable get() = if (AppThemeState.isDarkModeEnabled) Color(0xFFF87171) else Color(0xFFB91C1C)

    // ------------------------------------------------------------------------
    // 2. FORMAS UNIFICADAS (SHAPES CONSISTENTES)
    // ------------------------------------------------------------------------

    val ShapeHeroCard = RoundedCornerShape(26.dp)
    val ShapeSensorCard = RoundedCornerShape(22.dp)
    val ShapeButton = RoundedCornerShape(16.dp)
    val ShapeBadge = RoundedCornerShape(12.dp)
    val ShapeCircle = CircleShape

    // ------------------------------------------------------------------------
    // 3. ESPACIADOS HOMOLOGADOS (SPACING & PADDINGS)
    // ------------------------------------------------------------------------

    val ScreenPaddingHorizontal = 20.dp
    val ScreenPaddingVertical = 16.dp
    val CardPaddingHorizontal = 20.dp
    val CardPaddingVertical = 18.dp
    val ElementSpacingLarge = 20.dp
    val ElementSpacingMedium = 16.dp
    val ElementSpacingSmall = 10.dp
    val ElementSpacingTiny = 6.dp
}
