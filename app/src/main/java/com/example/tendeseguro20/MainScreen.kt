package com.example.tendeseguro20

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

// ============================================================================
// MODELOS DE DATOS Y ESTADO LOCAL
// ============================================================================

/**
 * Representa una lectura histórica de temperatura.
 */
data class TemperatureReading(
    val time: String,
    val value: Float,
)

/**
 * Estado general de los datos IoT de la aplicación.
 */
data class IotDataState(
    val temperature: Float = 24.5f,
    val humidity: Int = 82,
    val waterLevelPercent: Int = 45,
    val history: List<TemperatureReading> = listOf(
        TemperatureReading("08:00 AM", 21.0f),
        TemperatureReading("10:00 AM", 22.5f),
        TemperatureReading("12:00 PM", 23.8f),
        TemperatureReading("02:00 PM", 25.1f),
        TemperatureReading("04:00 PM", 24.5f),
    ),
    val lastUpdated: String = "Justo ahora",
)

// ============================================================================
// COLORES PASTEL Y PALETA VISUAL
// ============================================================================

private object PastelPalette {
    val Background = Color(0xFFFBF9F5)       // Crema / Gris muy claro suave
    val TextPrimary = Color(0xFF2C3E50)      // Azul oscuro / Gris antracita suave
    val TextSecondary = Color(0xFF7F8C8D)    // Gris medio suave

    // Tarjeta Temperatura (Azul Pastel Soft)
    val TempCardBackground = Color(0xFFEBF3FE)
    val TempCardBorder = Color(0xFFD0E2FF)
    val TempAccent = Color(0xFF4A90E2)

    // Tarjeta Humedad (Verde / Menta Pastel)
    val HumidCardBackground = Color(0xFFEBF8F2)
    val HumidCardBorder = Color(0xFFC7EFE0)

    // Tarjeta Agua / Sensor BX5 (Melocotón / Naranja Pastel)
    val WaterCardBackground = Color(0xFFFFF3EC)
    val WaterCardBorder = Color(0xFFFFE0D1)
    val WaterAccent = Color(0xFFE67E22)

    // Semáforo Pastel
    val TrafficGreen = Color(0xFF7BCD9B)
    val TrafficYellow = Color(0xFFFFD166)
    val TrafficRed = Color(0xFFEF476F)
}

// ============================================================================
// COMPOSABLE PRINCIPAL: MainScreen
// ============================================================================

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Estado principal simulado (Mock Data)
    var iotData by remember { mutableStateOf(IotDataState()) }

    // Estados de expansión de tarjetas
    var isTempExpanded by remember { mutableStateOf(value = false) }
    var isHumidExpanded by remember { mutableStateOf(value = false) }
    var isWaterExpanded by remember { mutableStateOf(value = false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PastelPalette.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header con Título y Botón de Actualizar
            HeaderSection(
                lastUpdated = iotData.lastUpdated,
                onRefreshClick = {
                    val rawTemp = Random.nextFloat() * 14f + 18f
                    val newTemp = (rawTemp * 10f).roundToInt() / 10.0f
                    val newHumid = Random.nextInt(45, 96)
                    val newWater = Random.nextInt(10, 95)
                    val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                    val updatedHistory = iotData.history.toMutableList().apply {
                        add(TemperatureReading(currentTime, newTemp))
                        if (size > 7) removeAt(0) // Mantener máximo 7 lecturas
                    }

                    iotData = IotDataState(
                        temperature = newTemp,
                        humidity = newHumid,
                        waterLevelPercent = newWater,
                        history = updatedHistory,
                        lastUpdated = currentTime
                    )
                }
            )

            // 1. TARJETA DE TEMPERATURA
            TemperatureCard(
                temperature = iotData.temperature,
                history = iotData.history,
                isExpanded = isTempExpanded,
                onCardClick = { isTempExpanded = !isTempExpanded }
            )

            // 2. TARJETA DE HUMEDAD (PREVENCIÓN DE LLUVIA Y SEMÁFORO)
            HumidityCard(
                humidity = iotData.humidity,
                isExpanded = isHumidExpanded,
                onCardClick = { isHumidExpanded = !isHumidExpanded }
            )

            // 3. TARJETA DE NIVEL DE AGUA (SENSOR BX5)
            WaterLevelCard(
                waterPercent = iotData.waterLevelPercent,
                isExpanded = isWaterExpanded,
                onCardClick = { isWaterExpanded = !isWaterExpanded }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================================================
// SECCIÓN DE ENCABEZADO Y REFRESH
// ============================================================================

@Composable
private fun HeaderSection(
    lastUpdated: String,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "TendeSeguro 2.0",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PastelPalette.TextPrimary
            )
            Text(
                text = "Monitoreo Preventivo IoT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PastelPalette.TextSecondary
            )
            Text(
                text = "Última act: $lastUpdated",
                fontSize = 11.sp,
                color = PastelPalette.TextSecondary.copy(alpha = 0.8f)
            )
        }

        Button(
            onClick = onRefreshClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PastelPalette.TempAccent,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar datos",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Actualizar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ============================================================================
// 1. TARJETA DE TEMPERATURA
// ============================================================================

@Composable
private fun TemperatureCard(
    temperature: Float,
    history: List<TemperatureReading>,
    isExpanded: Boolean,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, PastelPalette.TempCardBorder, RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PastelPalette.TempCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Fila principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PastelPalette.TempCardBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌡️", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Temperatura",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelPalette.TextPrimary
                        )
                        Text(
                            text = "Sensor Térmico Ambiental",
                            fontSize = 12.sp,
                            color = PastelPalette.TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$temperature °C",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PastelPalette.TempAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir detalles de temperatura",
                        tint = PastelPalette.TextSecondary
                    )
                }
            }

            // Contenido expandido: Histórico de Temperatura
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        color = PastelPalette.TempCardBorder,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Histórico de Temperatura",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPalette.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    history.forEach { reading ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reading.time,
                                fontSize = 12.sp,
                                color = PastelPalette.TextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { (reading.value.coerceIn(10f, 40f) - 10f) / 30f },
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PastelPalette.TempAccent,
                                    trackColor = PastelPalette.TempCardBorder,
                                    strokeCap = StrokeCap.Round
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${reading.value} °C",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PastelPalette.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. TARJETA DE HUMEDAD (PREVENCIÓN DE LLUVIA CON SEMÁFORO)
// ============================================================================

@Composable
private fun HumidityCard(
    humidity: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit
) {
    // Determinar nivel de alerta
    val (_, statusColor, isAlert) = when {
        humidity >= 80 -> Triple("Lluvia Inminente", PastelPalette.TrafficRed, true)
        humidity >= 65 -> Triple("Riesgo Moderado", PastelPalette.TrafficYellow, false)
        else -> Triple("Estable", PastelPalette.TrafficGreen, false)
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isAlert) PastelPalette.TrafficRed.copy(alpha = 0.5f) else PastelPalette.HumidCardBorder,
        label = "HumidCardBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, animatedBorderColor, RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PastelPalette.HumidCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PastelPalette.HumidCardBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💧", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Humedad Relativa",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelPalette.TextPrimary
                        )
                        Text(
                            text = "Prevención de Lluvia",
                            fontSize = 12.sp,
                            color = PastelPalette.TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$humidity %",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir alerta de humedad",
                        tint = PastelPalette.TextSecondary
                    )
                }
            }

            // Contenido expandido: Semáforo y Alerta
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        color = PastelPalette.HumidCardBorder,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sistema de Alerta Preventiva (Semáforo)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPalette.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Indicadores del Semáforo
                    TrafficLightSystem(currentHumidity = humidity)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Caja de mensaje de recomendación
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when {
                                    humidity >= 80 -> "¡ALERTA DE LLUVIA INMINENTE! Saturación de humedad del $humidity%. Recoja o proteja su ropa del tendedero."
                                    humidity >= 65 -> "HUMEDAD ELEVADA ($humidity%). Alta humedad en el ambiente, manténgase atento."
                                    else -> "ESTADO ESTABLE ($humidity%). Humedad adecuada. Bajo riesgo de precipitación."
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PastelPalette.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente del Sistema de Luces de Semáforo para la Humedad
 */
@Composable
private fun TrafficLightSystem(currentHumidity: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Verde (Baja / Estable < 65%)
        TrafficLightBulb(
            label = "Estable\n(<65%)",
            color = PastelPalette.TrafficGreen,
            isActive = currentHumidity < 65
        )

        // Amarillo (Moderado 65-79%)
        TrafficLightBulb(
            label = "Precaución\n(65-79%)",
            color = PastelPalette.TrafficYellow,
            isActive = currentHumidity in 65..79
        )

        // Rojo (Alerta Lluvia >= 80%)
        TrafficLightBulb(
            label = "Alerta Lluvia\n(≥80%)",
            color = PastelPalette.TrafficRed,
            isActive = currentHumidity >= 80
        )
    }
}

@Composable
private fun TrafficLightBulb(
    label: String,
    color: Color,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isActive) color else color.copy(alpha = 0.25f))
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) color else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) PastelPalette.TextPrimary else PastelPalette.TextSecondary
        )
    }
}

// ============================================================================
// 3. TARJETA DE NIVEL DE AGUA (SENSOR BX5)
// ============================================================================

@Composable
private fun WaterLevelCard(
    waterPercent: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit
) {
    val isCritical = waterPercent >= 75
    val statusLabel = if (isCritical) "Crítico" else "Normal"
    val statusColor = if (isCritical) PastelPalette.TrafficRed else PastelPalette.TrafficGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, PastelPalette.WaterCardBorder, RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PastelPalette.WaterCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PastelPalette.WaterCardBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌊", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Nivel de Agua",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelPalette.TextPrimary
                        )
                        Text(
                            text = "Módulo Sensor BX5",
                            fontSize = 12.sp,
                            color = PastelPalette.TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$waterPercent %",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PastelPalette.WaterAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir nivel de agua",
                        tint = PastelPalette.TextSecondary
                    )
                }
            }

            // Contenido expandido: Indicador de barra de nivel y estado operativo
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        color = PastelPalette.WaterCardBorder,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Indicador Visual del Nivel de Líquido (BX5)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPalette.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra de Nivel
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { waterPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = if (isCritical) PastelPalette.TrafficRed else PastelPalette.WaterAccent,
                            trackColor = PastelPalette.WaterCardBorder,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "0% (Seco)", fontSize = 10.sp, color = PastelPalette.TextSecondary)
                            Text(text = "50%", fontSize = 10.sp, color = PastelPalette.TextSecondary)
                            Text(text = "100% (Crítico)", fontSize = 10.sp, color = PastelPalette.TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Estado Operativo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .border(1.dp, PastelPalette.WaterCardBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Estado Operativo: $statusLabel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPalette.TextPrimary
                                )
                                Text(
                                    text = if (isCritical)
                                        "Sensor BX5 detecta acumulación excesiva de líquido en la canaleta o plataforma."
                                    else
                                        "Sensor BX5 operando dentro de los límites de humedad seguros.",
                                    fontSize = 11.sp,
                                    color = PastelPalette.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    TendeSeguro20Theme {
        MainScreen()
    }
}
