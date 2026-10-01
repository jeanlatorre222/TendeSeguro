package com.example.tendeseguro20

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de Almacenamiento Seguro de Tokens y Sesión de Usuario
 * Utiliza Android KeyStore y EncryptedSharedPreferences para encriptación AES-256.
 */
class SecureTokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "tendeseguro_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    }

    /**
     * Guarda el token de autenticación y el correo del usuario de forma encriptada.
     */
    fun saveAuthToken(token: String, refreshToken: String? = null, email: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            email?.let { putString(KEY_USER_EMAIL, it) }
            apply()
        }
    }

    /**
     * Obtiene el token de autenticación guardado.
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Obtiene el correo del usuario guardado.
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Guarda la preferencia de si el usuario ha habilitado inicio de sesión biométrico.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Retorna si el inicio de sesión biométrico está activo para este dispositivo.
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Borra los tokens y la sesión al cerrar sesión.
     */
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
