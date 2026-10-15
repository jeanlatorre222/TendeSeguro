package com.example.tendeseguro20

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

// ============================================================================
// MODELO CENTRAL DE SENSOR IOT (Single Source of Truth Model)
// ============================================================================

data class IotSensorModel(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: SensorType,
    val currentValue: Float,
    val unit: String,
    val customThreshold: Float,
    val calibrationOffset: Float = 0.0f,
    val isAlertEnabled: Boolean = true,
    val lastUpdated: String = "Justo ahora (En Caché)",
    val history: List<TemperatureReading> = listOf(
        TemperatureReading("08:00 AM", 21.0f),
        TemperatureReading("10:00 AM", 22.5f),
        TemperatureReading("12:00 PM", 23.8f),
        TemperatureReading("02:00 PM", 25.1f),
        TemperatureReading("04:00 PM", 24.5f),
    ),
)

// ============================================================================
// REPOSITORIO COMPARTIDO Y REACTIVO DE SENSORES IOT
// ============================================================================

object IotSensorRepository {

    private val initialSensors = listOf(
        IotSensorModel(
            id = "sensor_temp",
            name = "Temperatura",
            subtitle = "Sensor Térmico Ambiental",
            type = SensorType.TEMPERATURE,
            currentValue = 24.5f,
            unit = "°C",
            customThreshold = 32.0f,
        ),
        IotSensorModel(
            id = "sensor_humid",
            name = "Humedad Relativa",
            subtitle = "Prevención de Lluvia",
            type = SensorType.HUMIDITY,
            currentValue = 82.0f,
            unit = "%",
            customThreshold = 80.0f,
        ),
        IotSensorModel(
            id = "sensor_water",
            name = "Nivel de Agua",
            subtitle = "Módulo Sensor BX5",
            type = SensorType.WATER_BX5,
            currentValue = 45.0f,
            unit = "%",
            customThreshold = 75.0f,
        ),
    )

    private val _sensorsFlow = MutableStateFlow(initialSensors)
    val sensorsFlow: StateFlow<List<IotSensorModel>> = _sensorsFlow.asStateFlow()

    /**
     * Retorna un Flow reactivo para observar un sensor por su ID único compartido.
     */
    fun getSensorByIdFlow(sensorId: String): Flow<IotSensorModel?> {
        return _sensorsFlow.map { list ->
            list.find { it.id == sensorId } ?: list.find { it.type.name.equals(sensorId, ignoreCase = true) }
        }
    }

    /**
     * Actualiza la configuración y umbrales de un sensor específico por su ID.
     * La modificación se refleja de forma reactiva instantánea en la lista expuesta por StateFlow.
     */
    fun updateSensorConfig(
        sensorId: String,
        newThreshold: Float,
        newOffset: Float,
        isAlertEnabled: Boolean,
    ) {
        _sensorsFlow.update { currentList ->
            currentList.map { sensor ->
                if (sensor.id == sensorId || sensor.type.name.equals(sensorId, ignoreCase = true)) {
                    sensor.copy(
                        customThreshold = newThreshold,
                        calibrationOffset = newOffset,
                        isAlertEnabled = isAlertEnabled,
                    )
                } else {
                    sensor
                }
            }
        }
    }

    /**
     * Simula la actualización en tiempo real de lecturas recibidas desde el ESP32.
     */
    fun refreshSensorReadings() {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        _sensorsFlow.update { currentList ->
            currentList.map { sensor ->
                when (sensor.type) {
                    SensorType.TEMPERATURE -> {
                        val rawTemp = Random.nextFloat() * 14f + 18f
                        val newTemp = (rawTemp * 10f).roundToInt() / 10.0f
                        sensor.copy(currentValue = newTemp, lastUpdated = currentTime)
                    }

                    SensorType.HUMIDITY -> {
                        val newHumid = Random.nextInt(45, 96).toFloat()
                        sensor.copy(currentValue = newHumid, lastUpdated = currentTime)
                    }

                    SensorType.WATER_BX5 -> {
                        val newWater = Random.nextInt(10, 95).toFloat()
                        sensor.copy(currentValue = newWater, lastUpdated = currentTime)
                    }
                }
            }
        }
    }
}
