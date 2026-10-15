package com.example.tendeseguro20

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ============================================================================
// NIVELES DE SEVERIDAD Y MODELO DE ALERTA DE FIREBASE CLOUD MESSAGING (FCM)
// ============================================================================

enum class AlertSeverity(val label: String, val emoji: String) {
    CRITICAL("Crítico", "🚨"),
    WARNING("Advertencia", "⚠️"),
    INFO("Informativo", "ℹ️"),
}

data class AlertHistoryItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: AlertSeverity,
    val sensorType: SensorType,
    val isRead: Boolean = false,
)

// ============================================================================
// VIEWMODEL DEL CENTRO DE HISTORIAL DE ALERTAS
// ============================================================================

class AlertHistoryViewModel : ViewModel() {

    private val _alerts = MutableStateFlow<List<AlertHistoryItem>>(
        listOf(
            AlertHistoryItem(
                id = "alert_1",
                title = "¡Alerta de Lluvia Inminente!",
                description = "Sensor de humedad detectó una saturación del 88%. Tendedero protegido automáticamente.",
                timestamp = "Hoy, 10:45 AM",
                severity = AlertSeverity.CRITICAL,
                sensorType = SensorType.HUMIDITY,
            ),
            AlertHistoryItem(
                id = "alert_2",
                title = "Detección de Agua en Sensor BX5",
                description = "El módulo BX5 registró acumulación de líquido (62%) en la canaleta de desagüe.",
                timestamp = "Hoy, 08:30 AM",
                severity = AlertSeverity.WARNING,
                sensorType = SensorType.WATER_BX5,
            ),
            AlertHistoryItem(
                id = "alert_3",
                title = "Temperatura Ambiental Elevada",
                description = "Sensor térmico registró 33.2°C. Condiciones óptimas para secado rápido.",
                timestamp = "Ayer, 03:15 PM",
                severity = AlertSeverity.INFO,
                sensorType = SensorType.TEMPERATURE,
            ),
            AlertHistoryItem(
                id = "alert_4",
                title = "Reconexión Exitosa Módulo ESP32",
                description = "La señal inalámbrica con la estación IoT fue restablecida correctamente.",
                timestamp = "Ayer, 11:20 AM",
                severity = AlertSeverity.INFO,
                sensorType = SensorType.TEMPERATURE,
            ),
        )
    )
    val alerts: StateFlow<List<AlertHistoryItem>> = _alerts.asStateFlow()

    /**
     * Procesa y añade una nueva notificación entrante recibida mediante Firebase Cloud Messaging (FCM).
     */
    fun processIncomingFcmPayload(
        title: String,
        body: String,
        severityString: String = "CRITICAL",
        sensorTypeString: String = "HUMIDITY",
    ) {
        val severity = try {
            AlertSeverity.valueOf(severityString.uppercase())
        } catch (_: Exception) {
            AlertSeverity.WARNING
        }

        val sensorType = try {
            SensorType.valueOf(sensorTypeString.uppercase())
        } catch (_: Exception) {
            SensorType.HUMIDITY
        }

        val newAlert = AlertHistoryItem(
            id = "fcm_${System.currentTimeMillis()}",
            title = title,
            description = body,
            timestamp = "Justo ahora (FCM)",
            severity = severity,
            sensorType = sensorType,
        )

        _alerts.update { currentList ->
            listOf(newAlert) + currentList
        }
    }

    fun markAllAsRead() {
        _alerts.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun clearAlerts() {
        _alerts.value = emptyList()
    }
}
