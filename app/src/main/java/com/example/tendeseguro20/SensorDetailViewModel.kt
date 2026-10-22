package com.example.tendeseguro20

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ============================================================================
// ENUMERACIÓN TIPADA DE TIPOS DE SENSOR
// ============================================================================

enum class SensorType(val displayName: String, val unit: String, val icon: String) {
    TEMPERATURE("Sensor Térmico Ambiental", "°C", "🌡️"),
    HUMIDITY("Sensor de Humedad y Lluvia", "%", "💧"),
    WATER_BX5("Módulo de Nivel de Agua BX5", "%", "🌊"),
}

// ============================================================================
// ESTADO DE LA VISTA DE DETALLE CON CÁLCULO DE OFFSET Y UMBRAL (SensorDetailUiState)
// ============================================================================

data class SensorDetailUiState(
    val sensorId: String = "sensor_temp",
    val sensorType: SensorType = SensorType.TEMPERATURE,
    val rawSensorValue: Float = 24.5f,
    val adjustedValue: Float = 24.5f,
    val formattedDisplayValue: String = "24.5 °C",
    val statusLabel: String = "Temperatura Normal",
    val isAlertState: Boolean = false,
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
// VIEWMODEL CON LÓGICA DE OFFSET, UMBRALES Y BÚSQUEDA POR ID
// ============================================================================

class SensorDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Extrae el ID único del sensor pasado por la ruta de navegación
    val sensorId: String = savedStateHandle.get<String>("sensorId")
        ?: savedStateHandle.get<String>("sensorType")
        ?: "sensor_temp"

    private val _uiState = MutableStateFlow(SensorDetailUiState(sensorId = sensorId))
    val uiState: StateFlow<SensorDetailUiState> = _uiState.asStateFlow()

    init {
        // Suscripción reactiva al modelo unificado en IotSensorRepository
        viewModelScope.launch {
            IotSensorRepository.getSensorByIdFlow(sensorId).collect { sensorModel ->
                if (sensorModel != null) {
                    val rawVal = sensorModel.currentValue
                    val threshold = sensorModel.customThreshold
                    val offset = sensorModel.calibrationOffset

                    val (adjustedVal, statusText, isAlert) = calculateAdjustedState(
                        sensorType = sensorModel.type,
                        rawSensorValue = rawVal,
                        threshold = threshold,
                        offset = offset,
                    )

                    _uiState.update { current ->
                        current.copy(
                            sensorId = sensorModel.id,
                            sensorType = sensorModel.type,
                            rawSensorValue = rawVal,
                            adjustedValue = adjustedVal,
                            formattedDisplayValue = "$adjustedVal ${sensorModel.unit}",
                            statusLabel = statusText,
                            isAlertState = isAlert,
                            customThresholdInput = threshold.toString(),
                            calibrationOffsetInput = offset.toString(),
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

    /**
     * Invocado desde la UI para guardar y recalcular inmediatamente los estados
     * aplicando la fórmula: ValorFinal = ValorSensor + OffsetCalibracion.
     */
    fun guardarConfiguracion(nuevoUmbralInput: String, nuevoOffsetInput: String) {
        val parsedThreshold = nuevoUmbralInput.toFloatOrNull() ?: 30.0f
        val parsedOffset = nuevoOffsetInput.toFloatOrNull() ?: 0.0f
        val alertEnabled = _uiState.value.isAlertEnabled

        val rawVal = _uiState.value.rawSensorValue
        val sensorType = _uiState.value.sensorType

        // Recálculo inmediato con la fórmula: ValorFinal = ValorSensor + OffsetCalibracion
        val (adjustedVal, statusText, isAlert) = calculateAdjustedState(
            sensorType = sensorType,
            rawSensorValue = rawVal,
            threshold = parsedThreshold,
            offset = parsedOffset,
        )

        // Actualiza el repositorio global para persistir los cambios ante navegación
        IotSensorRepository.updateSensorConfig(
            sensorId = sensorId,
            newThreshold = parsedThreshold,
            newOffset = parsedOffset,
            isAlertEnabled = alertEnabled,
        )

        // Emite el nuevo estado ajustado en el StateFlow
        _uiState.update {
            it.copy(
                customThresholdInput = nuevoUmbralInput,
                calibrationOffsetInput = nuevoOffsetInput,
                adjustedValue = adjustedVal,
                formattedDisplayValue = "$adjustedVal ${sensorType.unit}",
                statusLabel = statusText,
                isAlertState = isAlert,
                isSaving = false,
                saveMessage = "Configuración guardada. Lectura ajustada con offset: $adjustedVal ${sensorType.unit} 💾",
            )
        }
    }

    fun saveConfiguration() {
        guardarConfiguracion(
            nuevoUmbralInput = _uiState.value.customThresholdInput,
            nuevoOffsetInput = _uiState.value.calibrationOffsetInput,
        )
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }

    /**
     * Lógica de evaluación y recálculo:
     * 1. ValorFinal = ValorSensor + OffsetCalibracion
     * 2. Humedad: Si Humedad Ajustada >= Umbral -> "Humedad Alta / Riesgo de Lluvia"
     * 3. Temperatura: Si Temperatura Ajustada <= Umbral -> "Temperatura Bajo el Límite"
     * 4. Agua BX5: Si Agua Ajustada >= Umbral -> "Nivel Crítico de Agua BX5"
     */
    private fun calculateAdjustedState(
        sensorType: SensorType,
        rawSensorValue: Float,
        threshold: Float,
        offset: Float,
    ): Triple<Float, String, Boolean> {
        val rawAdjusted = rawSensorValue + offset
        val adjustedValue = (rawAdjusted * 10f).roundToInt() / 10.0f

        return when (sensorType) {
            SensorType.HUMIDITY -> {
                if (adjustedValue >= threshold) {
                    Triple(adjustedValue, "Humedad Alta / Riesgo de Lluvia", true)
                } else {
                    Triple(adjustedValue, "Humedad Normal", false)
                }
            }

            SensorType.TEMPERATURE -> {
                if (adjustedValue <= threshold) {
                    Triple(adjustedValue, "Temperatura Bajo el Límite", true)
                } else {
                    Triple(adjustedValue, "Temperatura Normal", false)
                }
            }

            SensorType.WATER_BX5 -> {
                if (adjustedValue >= threshold) {
                    Triple(adjustedValue, "Nivel Crítico de Agua BX5", true)
                } else {
                    Triple(adjustedValue, "Nivel Normal de Agua", false)
                }
            }
        }
    }
}
