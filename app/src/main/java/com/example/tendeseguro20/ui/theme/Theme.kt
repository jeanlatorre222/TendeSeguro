package com.example.tendeseguro20.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ============================================================================
// ESQUEMA DE COLORES M3 - MODO CLARO Y OSCURO PROFESIONAL
// ============================================================================

private val PastelLightColorScheme = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBF3FE),
    onPrimaryContainer = Color(0xFF1D4ED8),
    secondary = Color(0xFF15803D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBF8F2),
    onSecondaryContainer = Color(0xFF15803D),
    background = Color(0xFFFAF8F5),
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFEBF3FE),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFD0E2FF),
    error = Color(0xFFB91C1C),
    onError = Color.White,
    errorContainer = Color(0xFFFFF0ED),
    onErrorContainer = Color(0xFFB91C1C),
)

private val PastelDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = Color(0xFF4ADE80),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = Color(0xFF86EFAC),
    background = Color(0xFF121212), // Gris oscuro profesional #121212
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E1E1E),    // Tarjetas #1E1E1E
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF252525), // Tarjetas secundarias #252525
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF333333),     // Bordes en modo oscuro
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFECACA),
)

@Composable
fun TendeSeguro20Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> PastelDarkColorScheme
        else -> PastelLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
