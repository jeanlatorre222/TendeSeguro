package com.example.tendeseguro20

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tendeseguro_settings")

/**
 * Gestor de preferencias para Onboarding y Recorrido de Funciones (App Tour) usando DataStore.
 */
class OnboardingPreferences(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
        private val APP_TOUR_COMPLETED_KEY = booleanPreferencesKey("has_completed_app_tour")
    }

    /**
     * Flow que emite true si el usuario ya completó el onboarding inicial.
     */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }

    /**
     * Flow que emite true si el usuario ya completó el Recorrido de Funciones (App Tour).
     */
    val hasCompletedAppTour: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[APP_TOUR_COMPLETED_KEY] ?: false
        }

    /**
     * Guarda la bandera lógica indicando que el onboarding ha sido completado.
     */
    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    /**
     * Guarda la bandera lógica indicando que el App Tour post-login ha sido completado.
     */
    suspend fun setAppTourCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[APP_TOUR_COMPLETED_KEY] = completed
        }
    }
}
