package com.example.tendeseguro20

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// VISTA DE PROFUNDIDAD (DETALLES Y FORMULARIO) CON PRESERVACIÓN DE ESTADO
// ============================================================================

@Composable
fun SensorDetailScreen(
    viewModel: SensorDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveMessage) {
        uiState.saveMessage?.let { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSaveMessage()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PastelDesignSystem.Background,
    ) {
        Scaffold(
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
                    .verticalScroll(rememberScrollState())
                    .imePadding(), // PREVENCIÓN DE SOLAPAMIENTO CON TECLADO
                verticalArrangement = Arrangement.spacedBy(PastelDesignSystem.ElementSpacingMedium),
            ) {
                // Header con botón Atrás
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Regresar a la pantalla anterior"
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = PastelDesignSystem.TextPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Detalle del Sensor",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelDesignSystem.TextPrimary,
                        )
                        Text(
                            text = uiState.sensorType.displayName,
                            fontSize = 12.sp,
                            color = PastelDesignSystem.TextSecondary,
                        )
                    }
                }

                // TARJETA DE ESTADO ACTUAL
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PastelDesignSystem.ShapeHeroCard)
                        .border(1.dp, PastelDesignSystem.TempBorder, PastelDesignSystem.ShapeHeroCard)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Estado actual del ${uiState.sensorType.displayName}: ${uiState.currentValue}. Estado operativo ${uiState.statusLabel}."
                        },
                    shape = PastelDesignSystem.ShapeHeroCard,
                    colors = CardDefaults.cardColors(containerColor = PastelDesignSystem.TempBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(PastelDesignSystem.ShapeCircle)
                                    .background(PastelDesignSystem.TempBorder),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = uiState.sensorType.icon, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Valor en Tiempo Real",
                                    fontSize = 13.sp,
                                    color = PastelDesignSystem.TextSecondary,
                                )
                                Text(
                                    text = uiState.currentValue,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PastelDesignSystem.TempAccent,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(PastelDesignSystem.ShapeBadge)
                                .background(PastelDesignSystem.TrafficGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = uiState.statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelDesignSystem.TrafficGreen,
                            )
                        }
                    }
                }

                // FORMULARIO ROBUSTO DE CONFIGURACIÓN DE UMBRALES
                Text(
                    text = "Ajuste de Umbral y Calibración",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelDesignSystem.TextPrimary,
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PastelDesignSystem.ShapeSensorCard)
                        .border(1.dp, PastelDesignSystem.TempBorder, PastelDesignSystem.ShapeSensorCard)
                        .animateContentSize(),
                    shape = PastelDesignSystem.ShapeSensorCard,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Switch Activar Alerta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notificaciones de Alerta Activas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelDesignSystem.TextPrimary,
                                )
                                Text(
                                    text = "Recibir avisos push ante lecturas anómalas",
                                    fontSize = 11.sp,
                                    color = PastelDesignSystem.TextSecondary,
                                )
                            }
                            Switch(
                                checked = uiState.isAlertEnabled,
                                onCheckedChange = { viewModel.toggleAlertEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PastelDesignSystem.TempAccent,
                                ),
                            )
                        }

                        HorizontalDivider(color = PastelDesignSystem.TempBorder, thickness = 1.dp)

                        // Campo 1: Umbral Personalizado (Teclado Decimal)
                        OutlinedTextField(
                            value = uiState.customThresholdInput,
                            onValueChange = { viewModel.updateThresholdInput(it) },
                            label = { Text("Umbral Limite (${uiState.sensorType.unit})") },
                            leadingIcon = { Text("⚡", fontSize = 18.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                            shape = PastelDesignSystem.ShapeButton,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelDesignSystem.TempAccent,
                                unfocusedBorderColor = PastelDesignSystem.TempBorder,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Campo 2: Offset de Calibración
                        OutlinedTextField(
                            value = uiState.calibrationOffsetInput,
                            onValueChange = { viewModel.updateCalibrationOffsetInput(it) },
                            label = { Text("Offset de Calibración (${uiState.sensorType.unit})") },
                            leadingIcon = { Text("🛠️", fontSize = 18.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() },
                            ),
                            shape = PastelDesignSystem.ShapeButton,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelDesignSystem.TempAccent,
                                unfocusedBorderColor = PastelDesignSystem.TempBorder,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // BOTÓN GUARDAR CONFIGURACIÓN
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.saveConfiguration()
                            },
                            shape = PastelDesignSystem.ShapeButton,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PastelDesignSystem.TempAccent,
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar Configuración",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                // HISTÓRICO Y GRÁFICO RESUMIDO
                Text(
                    text = "Tendencia de Lecturas Recientes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelDesignSystem.TextPrimary,
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PastelDesignSystem.ShapeSensorCard)
                        .border(1.dp, PastelDesignSystem.TempBorder, PastelDesignSystem.ShapeSensorCard),
                    shape = PastelDesignSystem.ShapeSensorCard,
                    colors = CardDefaults.cardColors(containerColor = PastelDesignSystem.TempBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        uiState.history.forEach { reading ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = reading.time, fontSize = 11.sp, color = PastelDesignSystem.TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { (reading.value.coerceIn(10f, 40f) - 10f) / 30f },
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = PastelDesignSystem.TempAccent,
                                        trackColor = PastelDesignSystem.TempBorder,
                                        strokeCap = StrokeCap.Round,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${reading.value} ${uiState.sensorType.unit}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelDesignSystem.TextPrimary,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
