package com.example.tendeseguro20

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================================
// ENUMERACIÓN TIPADA DE TIPOS DE SENSOR
// ============================================================================

enum class SensorType(val displayName: String, val unit: String, val icon: String) {
    TEMPERATURE("Sensor Térmico Ambiental", "°C", "🌡️"),
    HUMIDITY("Sensor de Humedad y Lluvia", "%", "💧"),
    WATER_BX5("Módulo de Nivel de Agua BX5", "%", "🌊"),
}

// ============================================================================
// ESTADO DE LA VISTA DE DETALLE (SensorDetailUiState)
// ============================================================================

data class SensorDetailUiState(
    val sensorId: String = "sensor_temp",
    val sensorType: SensorType = SensorType.TEMPERATURE,
    val currentValue: String = "24.5 °C",
    val statusLabel: String = "Normal",
    val customThresholdInput: String = "32.0",
    val calibrationOffsetInput: String = "0.0",
    val isAlertEnabled: Boolean = true,
    val history: List<TemperatureReading> = listOf(
        TemperatureReading("08:00 AM", 21.0f),
        TemperatureReading("10:00 AM", 22.5f),
        TemperatureReading("12:00 PM", 23.8f),
        TemperatureReading("02:00 PM", 25.1f),
        TemperatureReading("04:00 PM", 24.5f),
    ),
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
)

// ============================================================================
// VIEWMODEL BÚSQUEDA DINÁMICA POR ID Y SANGRADO ÚNICO
// ============================================================================

class SensorDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Extrae de forma segura el identificador único del sensor pasado por navegación
    val sensorId: String = savedStateHandle.get<String>("sensorId")
        ?: savedStateHandle.get<String>("sensorType")
        ?: "sensor_temp"

    private val _uiState = MutableStateFlow(SensorDetailUiState(sensorId = sensorId))
    val uiState: StateFlow<SensorDetailUiState> = _uiState.asStateFlow()

    init {
        // Suscripción al sensor correspondiente en IotSensorRepository
        viewModelScope.launch {
            IotSensorRepository.getSensorByIdFlow(sensorId).collect { sensorModel ->
                if (sensorModel != null) {
                    _uiState.update { current ->
                        current.copy(
                            sensorId = sensorModel.id,
                            sensorType = sensorModel.type,
                            currentValue = "${sensorModel.currentValue} ${sensorModel.unit}",
                            statusLabel = when {
                                sensorModel.type == SensorType.HUMIDITY && sensorModel.currentValue >= 80f -> "Lluvia Inminente"
                                sensorModel.type == SensorType.WATER_BX5 && sensorModel.currentValue >= 75f -> "Crítico"
                                else -> "Normal"
                            },
                            customThresholdInput = sensorModel.customThreshold.toString(),
                            calibrationOffsetInput = sensorModel.calibrationOffset.toString(),
                            isAlertEnabled = sensorModel.isAlertEnabled,
                            history = sensorModel.history,
                        )
                    }
                }
            }
        }
    }

    fun updateThresholdInput(input: String) {
        _uiState.update { it.copy(customThresholdInput = input) }
    }

    fun updateCalibrationOffsetInput(input: String) {
        _uiState.update { it.copy(calibrationOffsetInput = input) }
    }

    fun toggleAlertEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAlertEnabled = enabled) }
    }

    fun saveConfiguration() {
        val parsedThreshold = _uiState.value.customThresholdInput.toFloatOrNull() ?: 30.0f
        val parsedOffset = _uiState.value.calibrationOffsetInput.toFloatOrNull() ?: 0.0f
        val alertEnabled = _uiState.value.isAlertEnabled

        // Actualiza el repositorio único compartido
        IotSensorRepository.updateSensorConfig(
            sensorId = sensorId,
            newThreshold = parsedThreshold,
            newOffset = parsedOffset,
            isAlertEnabled = alertEnabled,
        )

        _uiState.update {
            it.copy(
                isSaving = false,
                saveMessage = "Umbral de ${_uiState.value.sensorType.displayName} ($parsedThreshold ${_uiState.value.sensorType.unit}) guardado en el modelo global 💾",
            )
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }
}
