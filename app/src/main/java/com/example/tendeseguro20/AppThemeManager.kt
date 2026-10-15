package com.example.tendeseguro20

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ============================================================================
// GESTOR GLOBAL Y REACTIVO DEL TEMA VISUAL (AppThemeState)
// ============================================================================

/**
 * Mantiene el estado mutable global del tema de la aplicación.
 * Permite que al alternar el Switch en Ajustes, Jetpack Compose recomponga
 * instantáneamente la raíz TendeSeguro20Theme en toda la jerarquía de vistas.
 */
object AppThemeState {
    var isDarkModeEnabled by mutableStateOf(false)

    fun toggleDarkMode(enabled: Boolean) {
        isDarkModeEnabled = enabled
    }
}
