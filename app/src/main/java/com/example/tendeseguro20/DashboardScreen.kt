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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

// ============================================================================
// ESTADO GENERAL DE DASHBOARD
// ============================================================================

data class DashboardState(
    val temperature: Float = 24.5f,
    val humidity: Int = 82,
    val waterLevelPercent: Int = 45,
    val isRackProtected: Boolean = false,
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
// PALETA VISUAL PASTEL
// ============================================================================

private object DashboardPastel {
    val Background = Color(0xFFFAF8F5)
    val TextPrimary = Color(0xFF2C3E50)
    val TextSecondary = Color(0xFF7F8C8D)

    val TempCardBackground = Color(0xFFEBF3FE)
    val TempCardBorder = Color(0xFFD0E2FF)
    val TempAccent = Color(0xFF4A90E2)

    val HumidCardBackground = Color(0xFFEBF8F2)
    val HumidCardBorder = Color(0xFFC7EFE0)

    val WaterCardBackground = Color(0xFFFFF3EC)
    val WaterCardBorder = Color(0xFFFFE0D1)
    val WaterAccent = Color(0xFFE67E22)

    val HeroSafeBackground = Color(0xFFEAFAF1)
    val HeroSafeBorder = Color(0xFFA2E8DD)
    val HeroWarningBackground = Color(0xFFFFF0ED)
    val HeroWarningBorder = Color(0xFFFFC4B8)

    val TrafficGreen = Color(0xFF7BCD9B)
    val TrafficYellow = Color(0xFFFFD166)
    val TrafficRed = Color(0xFFEF476F)
}

// ============================================================================
// COMPOSABLE PRINCIPAL: DashboardScreen CON CrucialStateContainer
// ============================================================================

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null,
) {
    var uiState by remember { mutableStateOf<AppUiState<DashboardState>>(AppUiState.Loading()) }
    val coroutineScope = rememberCoroutineScope()

    fun fetchDashboardData() {
        coroutineScope.launch {
            uiState = AppUiState.Loading("Conectando con sensor ESP32...")
            delay(800)
            val rawTemp = Random.nextFloat() * 14f + 18f
            val newTemp = (rawTemp * 10f).roundToInt() / 10.0f
            val newHumid = Random.nextInt(45, 96)
            val newWater = Random.nextInt(10, 95)
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            uiState = AppUiState.Success(
                DashboardState(
                    temperature = newTemp,
                    humidity = newHumid,
                    waterLevelPercent = newWater,
                    lastUpdated = currentTime,
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        fetchDashboardData()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DashboardPastel.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: Título de App y Botón Actualizar
            DashboardHeader(
                lastUpdated = (uiState as? AppUiState.Success)?.data?.lastUpdated ?: "Cargando...",
                onRefreshClick = { fetchDashboardData() },
                onSettingsClick = onSettingsClick,
            )

            // Selector de Prueba para Conmutar Dinámicamente los Estados Cruciales
            StateTestSelector(
                currentState = uiState,
                onStateSelected = { selectedState -> uiState = selectedState }
            )

            // CONTENEDOR REACTIVO PRINCIPAL DE ESTADOS CRUCIALES
            CrucialStateContainer(
                state = uiState,
                onRetry = { fetchDashboardData() },
                onInitializeFirstItem = { fetchDashboardData() },
            ) { data ->
                DashboardSuccessView(
                    initialData = data,
                    onStateChange = { updated ->
                        uiState = AppUiState.Success(updated)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================================================
// BARRA SELECTORA DE ESTADOS PARA DEMOSTRACIÓN/PRUEBA
// ============================================================================

@Composable
private fun StateTestSelector(
    currentState: AppUiState<DashboardState>,
    onStateSelected: (AppUiState<DashboardState>) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, DashboardPastel.TempCardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StateChip(
            label = "Éxito",
            isSelected = currentState is AppUiState.Success,
            onClick = {
                onStateSelected(AppUiState.Success(DashboardState()))
            }
        )
        StateChip(
            label = "Carga",
            isSelected = currentState is AppUiState.Loading,
            onClick = {
                onStateSelected(AppUiState.Loading("Conectando con sensor ESP32..."))
            }
        )
        StateChip(
            label = "Vacío",
            isSelected = currentState is AppUiState.Empty,
            onClick = {
                onStateSelected(
                    AppUiState.Empty(
                        title = "Sin Datos de Sensores",
                        description = "Aún no has vinculado tu módulo de agua BX5 o no existen lecturas en la base de datos.",
                        actionLabel = "Vincular Módulo BX5"
                    )
                )
            }
        )
        StateChip(
            label = "Error ESP32",
            isSelected = currentState is AppUiState.Error,
            onClick = {
                onStateSelected(
                    AppUiState.Error(
                        userFriendlyMessage = "Pérdida de comunicación inalámbrica con el módulo ESP32. Comprueba que el dispositivo tenga alimentación.",
                        canRetry = true
                    )
                )
            }
        )
    }
}

@Composable
private fun StateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DashboardPastel.TempAccent else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else DashboardPastel.TextSecondary,
        )
    }
}

// ============================================================================
// VISTA ÉXITO DEL DASHBOARD
// ============================================================================

@Composable
private fun DashboardSuccessView(
    initialData: DashboardState,
    onStateChange: (DashboardState) -> Unit,
) {
    var isTempExpanded by remember { mutableStateOf(false) }
    var isHumidExpanded by remember { mutableStateOf(false) }
    var isWaterExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // ACCIÓN CENTRAL DEL USUARIO: Hero Banner de Control del Tendedero
        HeroActionCard(
            humidity = initialData.humidity,
            waterPercent = initialData.waterLevelPercent,
            isProtected = initialData.isRackProtected,
            onToggleProtection = {
                onStateChange(initialData.copy(isRackProtected = !initialData.isRackProtected))
            },
        )

        Text(
            text = "Sensores en Tiempo Real",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DashboardPastel.TextPrimary,
            modifier = Modifier.padding(top = 4.dp),
        )

        // 1. TARJETA INTERACTIVA DE TEMPERATURA
        TemperatureInteractiveCard(
            temperature = initialData.temperature,
            history = initialData.history,
            isExpanded = isTempExpanded,
            onCardClick = { isTempExpanded = !isTempExpanded },
        )

        // 2. TARJETA INTERACTIVA DE HUMEDAD (SEMÁFORO DE LLUVIA)
        HumidityInteractiveCard(
            humidity = initialData.humidity,
            isExpanded = isHumidExpanded,
            onCardClick = { isHumidExpanded = !isHumidExpanded },
        )

        // 3. TARJETA INTERACTIVA DE NIVEL DE AGUA (SENSOR BX5)
        WaterLevelInteractiveCard(
            waterPercent = initialData.waterLevelPercent,
            isExpanded = isWaterExpanded,
            onCardClick = { isWaterExpanded = !isWaterExpanded },
        )
    }
}

// ============================================================================
// HEADER DE DASHBOARD
// ============================================================================

@Composable
private fun DashboardHeader(
    lastUpdated: String,
    onRefreshClick: () -> Unit,
    onSettingsClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "TendeSeguro 2.0",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DashboardPastel.TextPrimary,
            )
            Text(
                text = "Panel de Monitoreo IoT",
                fontSize = 13.sp,
                color = DashboardPastel.TextSecondary,
            )
            Text(
                text = "Actualizado: $lastUpdated",
                fontSize = 10.sp,
                color = DashboardPastel.TextSecondary.copy(alpha = 0.8f),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onRefreshClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DashboardPastel.TempAccent,
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar sensores",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Actualizar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            if (onSettingsClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DashboardPastel.TempCardBorder)
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil y Ajustes",
                        tint = DashboardPastel.TempAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// ============================================================================
// ACCIÓN CENTRAL DEL USUARIO: HERO BANNER
// ============================================================================

@Composable
private fun HeroActionCard(
    humidity: Int,
    waterPercent: Int,
    isProtected: Boolean,
    onToggleProtection: () -> Unit,
) {
    val isRainRisk = humidity >= 80 || waterPercent >= 75
    val cardBg = if (isRainRisk) DashboardPastel.HeroWarningBackground else DashboardPastel.HeroSafeBackground
    val cardBorder = if (isRainRisk) DashboardPastel.HeroWarningBorder else DashboardPastel.HeroSafeBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.5.dp, cardBorder, RoundedCornerShape(26.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isRainRisk) DashboardPastel.TrafficRed.copy(alpha = 0.2f) else DashboardPastel.TrafficGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isProtected) "☂️" else if (isRainRisk) "🚨" else "👕",
                            fontSize = 24.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isProtected) "Tendedero Protegido" else if (isRainRisk) "¡Alerta de Lluvia!" else "Tendedero Expuesto",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardPastel.TextPrimary,
                        )
                        Text(
                            text = if (isProtected) "Cubierta desplegada de forma segura" else if (isRainRisk) "Riesgo alto de lluvia ($humidity%)" else "Condiciones estables para secado",
                            fontSize = 12.sp,
                            color = DashboardPastel.TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onToggleProtection,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProtected) DashboardPastel.TrafficGreen else if (isRainRisk) DashboardPastel.TrafficRed else DashboardPastel.TempAccent,
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Icon(
                    imageVector = if (isProtected) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isProtected) "Desplegar Tendedero" else "Proteger Tendedero Ahora",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ============================================================================
// 1. TARJETA DE TEMPERATURA
// ============================================================================

@Composable
private fun TemperatureInteractiveCard(
    temperature: Float,
    history: List<TemperatureReading>,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, DashboardPastel.TempCardBorder, RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardPastel.TempCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DashboardPastel.TempCardBorder),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🌡️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Temperatura",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardPastel.TextPrimary,
                        )
                        Text(
                            text = "Sensor Térmico",
                            fontSize = 12.sp,
                            color = DashboardPastel.TextSecondary,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$temperature °C",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DashboardPastel.TempAccent,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DashboardPastel.TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = DashboardPastel.TempCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Histórico de Temperatura",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardPastel.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    history.forEach { reading ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = reading.time, fontSize = 11.sp, color = DashboardPastel.TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { (reading.value.coerceIn(10f, 40f) - 10f) / 30f },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = DashboardPastel.TempAccent,
                                    trackColor = DashboardPastel.TempCardBorder,
                                    strokeCap = StrokeCap.Round,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${reading.value} °C",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DashboardPastel.TextPrimary,
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
// 2. TARJETA DE HUMEDAD (SEMÁFORO DE LLUVIA)
// ============================================================================

@Composable
private fun HumidityInteractiveCard(
    humidity: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
) {
    val (_, statusColor, isAlert) = when {
        humidity >= 80 -> Triple("Lluvia Inminente", DashboardPastel.TrafficRed, true)
        humidity >= 65 -> Triple("Riesgo Moderado", DashboardPastel.TrafficYellow, false)
        else -> Triple("Estable", DashboardPastel.TrafficGreen, false)
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isAlert) DashboardPastel.TrafficRed.copy(alpha = 0.5f) else DashboardPastel.HumidCardBorder,
        label = "HumidCardBorder",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, animatedBorderColor, RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardPastel.HumidCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DashboardPastel.HumidCardBorder),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "💧", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Humedad Relativa",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardPastel.TextPrimary,
                        )
                        Text(
                            text = "Prevención de Lluvia",
                            fontSize = 12.sp,
                            color = DashboardPastel.TextSecondary,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$humidity %",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DashboardPastel.TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = DashboardPastel.HumidCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sistema de Alerta Preventiva (Semáforo)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardPastel.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrafficLightItem("Estable (<65%)", DashboardPastel.TrafficGreen, humidity < 65)
                        TrafficLightItem("Precaución (65-79%)", DashboardPastel.TrafficYellow, humidity in 65..79)
                        TrafficLightItem("Alerta Lluvia (≥80%)", DashboardPastel.TrafficRed, humidity >= 80)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    humidity >= 80 -> "¡ALERTA DE LLUVIA INMINENTE! Saturación del $humidity%. Proteja su tendedero."
                                    humidity >= 65 -> "HUMEDAD ELEVADA ($humidity%). Manténgase atento."
                                    else -> "ESTADO ESTABLE ($humidity%). Sin riesgo inmediato de lluvia."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DashboardPastel.TextPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrafficLightItem(
    label: String,
    color: Color,
    isActive: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isActive) color else color.copy(alpha = 0.25f))
                .border(if (isActive) 2.dp else 1.dp, if (isActive) color else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) DashboardPastel.TextPrimary else DashboardPastel.TextSecondary,
        )
    }
}

// ============================================================================
// 3. TARJETA DE NIVEL DE AGUA (SENSOR BX5)
// ============================================================================

@Composable
private fun WaterLevelInteractiveCard(
    waterPercent: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
) {
    val isCritical = waterPercent >= 75
    val statusLabel = if (isCritical) "Crítico" else "Normal"
    val statusColor = if (isCritical) DashboardPastel.TrafficRed else DashboardPastel.TrafficGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, DashboardPastel.WaterCardBorder, RoundedCornerShape(22.dp))
            .clickable { onCardClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardPastel.WaterCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DashboardPastel.WaterCardBorder),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🌊", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nivel de Agua",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DashboardPastel.TextPrimary,
                        )
                        Text(
                            text = "Módulo Sensor BX5",
                            fontSize = 12.sp,
                            color = DashboardPastel.TextSecondary,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$waterPercent %",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DashboardPastel.WaterAccent,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DashboardPastel.TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = DashboardPastel.WaterCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Indicador Visual del Nivel de Líquido (BX5)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardPastel.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { waterPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isCritical) DashboardPastel.TrafficRed else DashboardPastel.WaterAccent,
                        trackColor = DashboardPastel.WaterCardBorder,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "0% (Seco)", fontSize = 10.sp, color = DashboardPastel.TextSecondary)
                        Text(text = "50%", fontSize = 10.sp, color = DashboardPastel.TextSecondary)
                        Text(text = "100% (Crítico)", fontSize = 10.sp, color = DashboardPastel.TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .border(1.dp, DashboardPastel.WaterCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCritical)
                                    "Módulo BX5 detecta acumulación excesiva de líquido en la plataforma."
                                else
                                    "Módulo BX5 operando dentro de los límites de seguridad.",
                                fontSize = 11.sp,
                                color = DashboardPastel.TextPrimary,
                            )
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
fun DashboardScreenPreview() {
    TendeSeguro20Theme {
        DashboardScreen()
    }
}
