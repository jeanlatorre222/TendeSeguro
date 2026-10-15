package com.example.tendeseguro20

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ============================================================================
// MICROINTERACCIÓN DE ESCALA REUTILIZABLE
// ============================================================================

@Composable
fun Modifier.pressScaleEffectDashboard(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "DashboardPressScaleAnimation",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

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
    val lastUpdated: String = "Justo ahora (En Caché)",
)

// ============================================================================
// COMPOSABLE PRINCIPAL: DashboardScreen (DATOS ESTÁTICOS AL NAVEGAR)
// ============================================================================

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: (() -> Unit)? = null,
    onSensorDetailClick: ((SensorType) -> Unit)? = null,
) {
    // Suscripción reactiva al Single Source of Truth en memoria del repositorio
    val sensorsList by IotSensorRepository.sensorsFlow.collectAsState()

    val tempSensor = sensorsList.find { it.type == SensorType.TEMPERATURE }
    val humidSensor = sensorsList.find { it.type == SensorType.HUMIDITY }
    val waterSensor = sensorsList.find { it.type == SensorType.WATER_BX5 }

    // Inicialización ESTÁTICA en estado de éxito con los últimos datos en memoria
    var uiState by remember {
        mutableStateOf<AppUiState<DashboardState>>(
            AppUiState.Success(
                DashboardState(
                    temperature = tempSensor?.currentValue ?: 24.5f,
                    humidity = (humidSensor?.currentValue ?: 82f).roundToInt(),
                    waterLevelPercent = (waterSensor?.currentValue ?: 45f).roundToInt(),
                    lastUpdated = tempSensor?.lastUpdated ?: "Justo ahora",
                )
            )
        )
    }

    var isOffline by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showFeedbackSnackbar(msg: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    /**
     * ACCIÓN MANUAL EXCLUSIVA: Se ejecuta ÚNICAMENTE cuando el usuario presiona el botón "Actualizar".
     * NO existe ningún LaunchedEffect(Unit) que recargue datos automáticamente al navegar o cambiar de pestaña.
     */
    fun fetchDashboardData() {
        coroutineScope.launch {
            if (isOffline) {
                showFeedbackSnackbar("Estás sin conexión. Mostrando datos cargados en caché 📡")
                return@launch
            }

            uiState = AppUiState.Loading("Conectando con sensor ESP32...")
            delay(600)

            // Recalcula y actualiza la fuente de datos global en memoria exclusivamente bajo demanda del usuario
            IotSensorRepository.refreshSensorReadings()

            uiState = AppUiState.Success(
                DashboardState(
                    temperature = tempSensor?.currentValue ?: 24.5f,
                    humidity = (humidSensor?.currentValue ?: 82f).roundToInt(),
                    waterLevelPercent = (waterSensor?.currentValue ?: 45f).roundToInt(),
                    lastUpdated = tempSensor?.lastUpdated ?: "Justo ahora",
                )
            )
            showFeedbackSnackbar("Lecturas de sensores actualizadas manualmente 🔄")
        }
    }

    // Sincronización reactiva del estado cuando cambia el repositorio global
    LaunchedEffect(sensorsList) {
        if (uiState is AppUiState.Success) {
            uiState = AppUiState.Success(
                DashboardState(
                    temperature = tempSensor?.currentValue ?: 24.5f,
                    humidity = (humidSensor?.currentValue ?: 82f).roundToInt(),
                    waterLevelPercent = (waterSensor?.currentValue ?: 45f).roundToInt(),
                    lastUpdated = tempSensor?.lastUpdated ?: "Justo ahora",
                )
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = PastelDesignSystem.TextPrimary,
                    contentColor = Color.White,
                    shape = PastelDesignSystem.ShapeButton,
                    modifier = Modifier.padding(16.dp),
                )
            }
        },
        containerColor = PastelDesignSystem.Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = PastelDesignSystem.ScreenPaddingHorizontal,
                    vertical = PastelDesignSystem.ScreenPaddingVertical,
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PastelDesignSystem.ElementSpacingMedium),
        ) {
            // Header
            DashboardHeader(
                lastUpdated = tempSensor?.lastUpdated ?: "Justo ahora",
                isOffline = isOffline,
                onToggleOffline = { isOffline = !isOffline },
                onRefreshClick = { fetchDashboardData() },
                onSettingsClick = onSettingsClick,
            )

            // Banner Sutil Sin Conexión
            AnimatedVisibility(
                visible = isOffline,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                OfflineBannerDashboard()
            }

            // Selector de Demostración
            StateTestSelector(
                currentState = uiState,
                onStateSelected = { selectedState -> uiState = selectedState },
            )

            // Contenedor de Estado Reactivo
            CrucialStateContainer(
                state = uiState,
                onRetry = { fetchDashboardData() },
                onInitializeFirstItem = { fetchDashboardData() },
            ) { data ->
                DashboardSuccessView(
                    initialData = data,
                    tempSensor = tempSensor,
                    humidSensor = humidSensor,
                    waterSensor = waterSensor,
                    onStateChange = { updated ->
                        uiState = AppUiState.Success(updated)
                    },
                    onShowSnackbar = { msg -> showFeedbackSnackbar(msg) },
                    onSensorDetailClick = onSensorDetailClick,
                )
            }

            Spacer(modifier = Modifier.height(PastelDesignSystem.ElementSpacingMedium))
        }
    }
}

// ============================================================================
// BANNER SUTIL SIN CONEXIÓN
// ============================================================================

@Composable
private fun OfflineBannerDashboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PastelDesignSystem.ShapeBadge)
            .background(PastelDesignSystem.AlertBackground)
            .border(1.dp, PastelDesignSystem.AlertBorder, PastelDesignSystem.ShapeBadge)
            .padding(14.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Aviso de sin conexión activo. Mostrando datos guardados en la memoria caché local."
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = PastelDesignSystem.AlertRed,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Estás sin conexión",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelDesignSystem.TextPrimary,
                )
                Text(
                    text = "Mostrando los últimos datos cargados en caché local.",
                    fontSize = 11.sp,
                    color = PastelDesignSystem.TextSecondary,
                )
            }
        }
    }
}

// ============================================================================
// BARRA SELECTORA DE ESTADOS
// ============================================================================

@Composable
private fun StateTestSelector(
    currentState: AppUiState<DashboardState>,
    onStateSelected: (AppUiState<DashboardState>) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PastelDesignSystem.ShapeBadge)
            .background(Color.White)
            .border(1.dp, PastelDesignSystem.TempBorder, PastelDesignSystem.ShapeBadge)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StateChip(
            label = "Éxito",
            isSelected = currentState is AppUiState.Success,
            onClick = { onStateSelected(AppUiState.Success(DashboardState())) },
        )
        StateChip(
            label = "Carga",
            isSelected = currentState is AppUiState.Loading,
            onClick = { onStateSelected(AppUiState.Loading("Conectando con sensor ESP32...")) },
        )
        StateChip(
            label = "Vacío",
            isSelected = currentState is AppUiState.Empty,
            onClick = {
                onStateSelected(
                    AppUiState.Empty(
                        title = "Sin Datos de Sensores",
                        description = "Aún no has vinculado tu módulo de agua BX5 o no existen lecturas en la base de datos.",
                        actionLabel = "Vincular Módulo BX5",
                    )
                )
            },
        )
        StateChip(
            label = "Error ESP32",
            isSelected = currentState is AppUiState.Error,
            onClick = {
                onStateSelected(
                    AppUiState.Error(
                        userFriendlyMessage = "No pudimos conectar con el módulo ESP32. Verifica que el dispositivo tenga alimentación e intenta de nuevo.",
                        canRetry = true,
                    )
                )
            },
        )
    }
}

@Composable
private fun StateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val chipInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .pressScaleEffectDashboard(chipInteractionSource)
            .clip(PastelDesignSystem.ShapeBadge)
            .background(if (isSelected) PastelDesignSystem.TempAccent else Color.Transparent)
            .clickable(interactionSource = chipInteractionSource, indication = null) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .semantics {
                contentDescription = "Estado de simulación $label. ${if (isSelected) "Seleccionado actualmente" else "Presiona para activar"}"
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else PastelDesignSystem.TextSecondary,
        )
    }
}

// ============================================================================
// VISTA ÉXITO DEL DASHBOARD
// ============================================================================

@Composable
private fun DashboardSuccessView(
    initialData: DashboardState,
    tempSensor: IotSensorModel?,
    humidSensor: IotSensorModel?,
    waterSensor: IotSensorModel?,
    onStateChange: (DashboardState) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onSensorDetailClick: ((SensorType) -> Unit)?,
) {
    var isTempExpanded by remember { mutableStateOf(false) }
    var isHumidExpanded by remember { mutableStateOf(false) }
    var isWaterExpanded by remember { mutableStateOf(false) }

    val currentTemp = tempSensor?.currentValue ?: initialData.temperature
    val currentHumid = (humidSensor?.currentValue ?: initialData.humidity.toFloat()).roundToInt()
    val currentWater = (waterSensor?.currentValue ?: initialData.waterLevelPercent.toFloat()).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(PastelDesignSystem.ElementSpacingMedium)) {
        // ACCIÓN CENTRAL HERO
        HeroActionCard(
            humidity = currentHumid,
            waterPercent = currentWater,
            isProtected = initialData.isRackProtected,
            onToggleProtection = {
                val newProtectedState = !initialData.isRackProtected
                onStateChange(initialData.copy(isRackProtected = newProtectedState))
                onShowSnackbar(
                    if (newProtectedState) "¡Tendedero protegido con éxito contra la lluvia! ☂️"
                    else "Cubierta del tendedero replegada. Modo de secado activo ☀️",
                )
            },
        )

        Text(
            text = "Sensores en Tiempo Real (Estado estático al navegar)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PastelDesignSystem.TextPrimary,
            modifier = Modifier.padding(top = 4.dp),
        )

        TemperatureInteractiveCard(
            temperature = currentTemp,
            history = tempSensor?.history ?: initialData.history,
            isExpanded = isTempExpanded,
            onCardClick = {
                if (onSensorDetailClick != null) {
                    onSensorDetailClick(SensorType.TEMPERATURE)
                } else {
                    isTempExpanded = !isTempExpanded
                    onShowSnackbar(if (isTempExpanded) "Histórico de temperatura desplegado 🌡️" else "Histórico contraído")
                }
            },
        )

        HumidityInteractiveCard(
            humidity = currentHumid,
            isExpanded = isHumidExpanded,
            onCardClick = {
                if (onSensorDetailClick != null) {
                    onSensorDetailClick(SensorType.HUMIDITY)
                } else {
                    isHumidExpanded = !isHumidExpanded
                    onShowSnackbar(if (isHumidExpanded) "Sistema de prevención de lluvia activo 💧" else "Alerta contraída")
                }
            },
        )

        WaterLevelInteractiveCard(
            waterPercent = currentWater,
            isExpanded = isWaterExpanded,
            onCardClick = {
                if (onSensorDetailClick != null) {
                    onSensorDetailClick(SensorType.WATER_BX5)
                } else {
                    isWaterExpanded = !isWaterExpanded
                    onShowSnackbar(if (isWaterExpanded) "Módulo indicador de agua BX5 desplegado 🌊" else "Indicador BX5 contraído")
                }
            },
        )
    }
}

// ============================================================================
// HEADER ACCESIBLE
// ============================================================================

@Composable
private fun DashboardHeader(
    lastUpdated: String,
    isOffline: Boolean,
    onToggleOffline: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: (() -> Unit)?,
) {
    val refreshInteractionSource = remember { MutableInteractionSource() }
    val profileInteractionSource = remember { MutableInteractionSource() }

    Column(verticalArrangement = Arrangement.spacedBy(PastelDesignSystem.ElementSpacingSmall)) {
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
                    color = PastelDesignSystem.TextPrimary,
                )
                Text(
                    text = "Panel de Monitoreo IoT",
                    fontSize = 13.sp,
                    color = PastelDesignSystem.TextSecondary,
                )
                Text(
                    text = "Actualizado: $lastUpdated",
                    fontSize = 10.sp,
                    color = PastelDesignSystem.TextSecondary.copy(alpha = 0.8f),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // BOTÓN ACTUALIZAR: Única acción que gatilla la recarga de datos
                Button(
                    onClick = onRefreshClick,
                    interactionSource = refreshInteractionSource,
                    shape = PastelDesignSystem.ShapeButton,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PastelDesignSystem.TempAccent,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .pressScaleEffectDashboard(refreshInteractionSource)
                        .semantics {
                            contentDescription = "Presiona para actualizar manualmente las lecturas de los sensores"
                        },
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Actualizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (onSettingsClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .pressScaleEffectDashboard(profileInteractionSource)
                            .size(38.dp)
                            .clip(PastelDesignSystem.ShapeCircle)
                            .background(PastelDesignSystem.TempBorder)
                            .clickable(interactionSource = profileInteractionSource, indication = null) { onSettingsClick() }
                            .semantics {
                                contentDescription = "Perfil del usuario y configuración de preferencias"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PastelDesignSystem.TempAccent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .clip(PastelDesignSystem.ShapeBadge)
                .background(Color.White)
                .border(1.dp, PastelDesignSystem.TempBorder, PastelDesignSystem.ShapeBadge)
                .clickable { onToggleOffline() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .semantics {
                    contentDescription = if (isOffline)
                        "Modo sin conexión activo. Presiona para reconectar en línea."
                    else
                        "Modo en línea activo. Presiona para simular estado sin conexión."
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(PastelDesignSystem.ShapeCircle)
                    .background(if (isOffline) PastelDesignSystem.TrafficRed else PastelDesignSystem.TrafficGreen),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOffline) "Modo: Sin Conexión (Click para Conectar)" else "Modo: En Línea (Click para Simular Offline)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PastelDesignSystem.TextSecondary,
            )
        }
    }
}

// ============================================================================
// ACCIÓN CENTRAL HERO
// ============================================================================

@Composable
private fun HeroActionCard(
    humidity: Int,
    waterPercent: Int,
    isProtected: Boolean,
    onToggleProtection: () -> Unit,
) {
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isRainRisk = humidity >= 80 || waterPercent >= 75
    val cardBg = if (isRainRisk) PastelDesignSystem.HeroWarningBackground else PastelDesignSystem.HeroSafeBackground
    val cardBorder = if (isRainRisk) PastelDesignSystem.HeroWarningBorder else PastelDesignSystem.HeroSafeBorder

    val talkBackHeroLabel = if (isProtected) {
        "Estado actual: Tendedero protegido. Cubierta desplegada de forma segura."
    } else if (isRainRisk) {
        "Alerta de lluvia inminente con humedad del $humidity por ciento. Tendedero expuesto."
    } else {
        "Estado actual: Tendedero expuesto con condiciones estables para el secado."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PastelDesignSystem.ShapeHeroCard)
            .border(1.5.dp, cardBorder, PastelDesignSystem.ShapeHeroCard)
            .animateContentSize()
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackHeroLabel
            },
        shape = PastelDesignSystem.ShapeHeroCard,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(PastelDesignSystem.CardPaddingHorizontal),
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
                            .clip(PastelDesignSystem.ShapeCircle)
                            .background(if (isRainRisk) PastelDesignSystem.TrafficRed.copy(alpha = 0.2f) else PastelDesignSystem.TrafficGreen.copy(alpha = 0.2f)),
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
                            color = PastelDesignSystem.TextPrimary,
                        )
                        Text(
                            text = if (isProtected) "Cubierta desplegada de forma segura" else if (isRainRisk) "Riesgo alto de lluvia ($humidity%)" else "Condiciones estables para secado",
                            fontSize = 12.sp,
                            color = PastelDesignSystem.TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isProtected) {
                OutlinedButton(
                    onClick = onToggleProtection,
                    interactionSource = cardInteractionSource,
                    shape = PastelDesignSystem.ShapeButton,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PastelDesignSystem.TextPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .pressScaleEffectDashboard(cardInteractionSource)
                        .semantics {
                            contentDescription = "Presiona para desplegar el tendedero y permitir el secado al aire libre"
                        },
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PastelDesignSystem.TrafficGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Desplegar Tendedero",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Button(
                    onClick = onToggleProtection,
                    interactionSource = cardInteractionSource,
                    shape = PastelDesignSystem.ShapeButton,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRainRisk) PastelDesignSystem.TrafficRed else PastelDesignSystem.TempAccent,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .pressScaleEffectDashboard(cardInteractionSource)
                        .semantics {
                            contentDescription = "Presiona para proteger inmediatamente el tendedero contra la lluvia"
                        },
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Proteger Tendedero Ahora",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ============================================================================
// 1. TARJETA ACCESIBLE DE TEMPERATURA
// ============================================================================

@Composable
private fun TemperatureInteractiveCard(
    temperature: Float,
    history: List<TemperatureReading>,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
) {
    val cardInteractionSource = remember { MutableInteractionSource() }

    val talkBackTempLabel = "Sensor de temperatura ambiental actual: $temperature grados centígrados. Presiona para ver detalle del sensor térmico."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleEffectDashboard(cardInteractionSource)
            .clip(PastelDesignSystem.ShapeSensorCard)
            .border(1.dp, PastelDesignSystem.TempBorder, PastelDesignSystem.ShapeSensorCard)
            .clickable(interactionSource = cardInteractionSource, indication = null) { onCardClick() }
            .animateContentSize()
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackTempLabel
            },
        shape = PastelDesignSystem.ShapeSensorCard,
        colors = CardDefaults.cardColors(containerColor = PastelDesignSystem.TempBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(PastelDesignSystem.CardPaddingHorizontal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(PastelDesignSystem.ShapeCircle)
                            .background(PastelDesignSystem.TempBorder),
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
                            color = PastelDesignSystem.TextPrimary,
                        )
                        Text(
                            text = "Sensor Térmico",
                            fontSize = 12.sp,
                            color = PastelDesignSystem.TextSecondary,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$temperature °C",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PastelDesignSystem.TempAccent,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Ver detalle de temperatura",
                        tint = PastelDesignSystem.TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = PastelDesignSystem.TempBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Histórico de Temperatura",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelDesignSystem.TextPrimary,
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
                            Text(text = reading.time, fontSize = 11.sp, color = PastelDesignSystem.TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { (reading.value.coerceIn(10f, 40f) - 10f) / 30f },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PastelDesignSystem.TempAccent,
                                    trackColor = PastelDesignSystem.TempBorder,
                                    strokeCap = StrokeCap.Round,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${reading.value} °C",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PastelDesignSystem.TextPrimary,
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
// 2. TARJETA ACCESIBLE DE HUMEDAD
// ============================================================================

@Composable
private fun HumidityInteractiveCard(
    humidity: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
) {
    val cardInteractionSource = remember { MutableInteractionSource() }

    val (statusText, statusColor, isAlert) = when {
        humidity >= 80 -> Triple("Lluvia Inminente", PastelDesignSystem.TrafficRed, true)
        humidity >= 65 -> Triple("Riesgo Moderado", PastelDesignSystem.TrafficYellow, false)
        else -> Triple("Estable", PastelDesignSystem.TrafficGreen, false)
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isAlert) PastelDesignSystem.TrafficRed.copy(alpha = 0.5f) else PastelDesignSystem.HumidBorder,
        label = "HumidCardBorder",
    )

    val talkBackHumidLabel = "Sensor de humedad relativa: $humidity por ciento. Estado de prevención: $statusText. Presiona para ver detalle de humedad."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleEffectDashboard(cardInteractionSource)
            .clip(PastelDesignSystem.ShapeSensorCard)
            .border(1.dp, animatedBorderColor, PastelDesignSystem.ShapeSensorCard)
            .clickable(interactionSource = cardInteractionSource, indication = null) { onCardClick() }
            .animateContentSize()
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackHumidLabel
            },
        shape = PastelDesignSystem.ShapeSensorCard,
        colors = CardDefaults.cardColors(containerColor = PastelDesignSystem.HumidBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(PastelDesignSystem.CardPaddingHorizontal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(PastelDesignSystem.ShapeCircle)
                            .background(PastelDesignSystem.HumidBorder),
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
                            color = PastelDesignSystem.TextPrimary,
                        )
                        Text(
                            text = "Prevención de Lluvia",
                            fontSize = 12.sp,
                            color = PastelDesignSystem.TextSecondary,
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
                        contentDescription = "Ver detalle de humedad",
                        tint = PastelDesignSystem.TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = PastelDesignSystem.HumidBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sistema de Alerta Preventiva (Semáforo)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelDesignSystem.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PastelDesignSystem.ShapeBadge)
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrafficLightItem("Estable (<65%)", PastelDesignSystem.TrafficGreen, humidity < 65)
                        TrafficLightItem("Precaución (65-79%)", PastelDesignSystem.TrafficYellow, humidity in 65..79)
                        TrafficLightItem("Alerta Lluvia (≥80%)", PastelDesignSystem.TrafficRed, humidity >= 80)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PastelDesignSystem.ShapeBadge)
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), PastelDesignSystem.ShapeBadge)
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
                                color = PastelDesignSystem.TextPrimary,
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Luz de semáforo $label. ${if (isActive) "Activa actualmente" else "Inactiva"}"
        },
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(PastelDesignSystem.ShapeCircle)
                .background(if (isActive) color else color.copy(alpha = 0.25f))
                .border(if (isActive) 2.dp else 1.dp, if (isActive) color else Color.Transparent, PastelDesignSystem.ShapeCircle),
            contentAlignment = Alignment.Center,
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(PastelDesignSystem.ShapeCircle)
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
            color = if (isActive) PastelDesignSystem.TextPrimary else PastelDesignSystem.TextSecondary,
        )
    }
}

// ============================================================================
// 3. TARJETA ACCESIBLE DE NIVEL DE AGUA (SENSOR BX5)
// ============================================================================

@Composable
private fun WaterLevelInteractiveCard(
    waterPercent: Int,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
) {
    val cardInteractionSource = remember { MutableInteractionSource() }

    val isCritical = waterPercent >= 75
    val statusLabel = if (isCritical) "Crítico" else "Normal"
    val statusColor = if (isCritical) PastelDesignSystem.TrafficRed else PastelDesignSystem.TrafficGreen

    val talkBackWaterLabel = "Sensor de nivel de agua módulo BX5 al $waterPercent por ciento de capacidad. Estado $statusLabel. Presiona para ver detalle del módulo BX5."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressScaleEffectDashboard(cardInteractionSource)
            .clip(PastelDesignSystem.ShapeSensorCard)
            .border(1.dp, PastelDesignSystem.WaterBorder, PastelDesignSystem.ShapeSensorCard)
            .clickable(interactionSource = cardInteractionSource, indication = null) { onCardClick() }
            .animateContentSize()
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackWaterLabel
            },
        shape = PastelDesignSystem.ShapeSensorCard,
        colors = CardDefaults.cardColors(containerColor = PastelDesignSystem.WaterBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(PastelDesignSystem.CardPaddingHorizontal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(PastelDesignSystem.ShapeCircle)
                            .background(PastelDesignSystem.WaterBorder),
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
                            color = PastelDesignSystem.TextPrimary,
                        )
                        Text(
                            text = "Módulo Sensor BX5",
                            fontSize = 12.sp,
                            color = PastelDesignSystem.TextSecondary,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(PastelDesignSystem.ShapeBadge)
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
                        color = PastelDesignSystem.WaterAccent,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Ver detalle del nivel de agua BX5",
                        tint = PastelDesignSystem.TextSecondary,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = PastelDesignSystem.WaterBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Indicador Visual del Nivel de Líquido (BX5)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelDesignSystem.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { waterPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .semantics {
                                contentDescription = "Barra indicadora de nivel de líquido al $waterPercent por ciento"
                            },
                        color = if (isCritical) PastelDesignSystem.TrafficRed else PastelDesignSystem.WaterAccent,
                        trackColor = PastelDesignSystem.WaterBorder,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "0% (Seco)", fontSize = 10.sp, color = PastelDesignSystem.TextSecondary)
                        Text(text = "50%", fontSize = 10.sp, color = PastelDesignSystem.TextSecondary)
                        Text(text = "100% (Crítico)", fontSize = 10.sp, color = PastelDesignSystem.TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PastelDesignSystem.ShapeBadge)
                            .background(Color.White.copy(alpha = 0.7f))
                            .border(1.dp, PastelDesignSystem.WaterBorder, PastelDesignSystem.ShapeBadge)
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.CheckCircle,
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
                                color = PastelDesignSystem.TextPrimary,
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
