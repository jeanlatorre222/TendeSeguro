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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme

// ============================================================================
// CENTRO DE HISTORIAL DE ALERTAS (LazyColumn + FCM Base)
// ============================================================================

@Composable
fun AlertHistoryScreen(
    viewModel: AlertHistoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alerts by viewModel.alerts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PastelDesignSystem.Background,
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = PastelDesignSystem.Background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(
                        horizontal = PastelDesignSystem.ScreenPaddingHorizontal,
                        vertical = PastelDesignSystem.ScreenPaddingVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(PastelDesignSystem.ElementSpacingMedium),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.semantics {
                                contentDescription = "Regresar al Dashboard"
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
                                text = "Historial de Alertas",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelDesignSystem.TextPrimary,
                            )
                            Text(
                                text = "Notificaciones IoT & FCM",
                                fontSize = 12.sp,
                                color = PastelDesignSystem.TextSecondary,
                            )
                        }
                    }

                    if (alerts.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearAlerts() },
                            modifier = Modifier.semantics {
                                contentDescription = "Limpiar todo el historial de alertas"
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = PastelDesignSystem.AlertRed,
                            )
                        }
                    }
                }

                if (alerts.isEmpty()) {
                    // Estado Vacío de Alertas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(PastelDesignSystem.ShapeCircle)
                                    .background(PastelDesignSystem.HumidBorder),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = PastelDesignSystem.HumidAccent,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin Alertas Registradas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelDesignSystem.TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No se han detectado eventos críticos de lluvia o temperatura recientemente.",
                                fontSize = 12.sp,
                                color = PastelDesignSystem.TextSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    // LazyColumn de Alertas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = alerts,
                            key = { it.id },
                        ) { alert ->
                            AlertCardItem(alert = alert)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TARJETA INDIVIDUAL DE ALERTA CON BADGE DE SEVERIDAD Y TIMESTAMP
// ============================================================================

@Composable
private fun AlertCardItem(alert: AlertHistoryItem) {
    val (severityColor, severityBg) = when (alert.severity) {
        AlertSeverity.CRITICAL -> Pair(PastelDesignSystem.TrafficRed, PastelDesignSystem.HeroWarningBackground)
        AlertSeverity.WARNING -> Pair(PastelDesignSystem.TrafficYellow, PastelDesignSystem.WaterBackground)
        AlertSeverity.INFO -> Pair(PastelDesignSystem.TempAccent, PastelDesignSystem.TempBackground)
    }

    val talkBackLabel = "Alerta ${alert.severity.label}: ${alert.title}. ${alert.description}. Marca de tiempo: ${alert.timestamp}."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PastelDesignSystem.ShapeSensorCard)
            .border(1.dp, severityColor.copy(alpha = 0.4f), PastelDesignSystem.ShapeSensorCard)
            .animateContentSize()
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackLabel
            },
        shape = PastelDesignSystem.ShapeSensorCard,
        colors = CardDefaults.cardColors(containerColor = severityBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = alert.severity.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = alert.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelDesignSystem.TextPrimary,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(PastelDesignSystem.ShapeBadge)
                        .background(severityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = alert.severity.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.description,
                fontSize = 12.sp,
                color = PastelDesignSystem.TextSecondary,
                lineHeight = 17.sp,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sensor: ${alert.sensorType.displayName}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PastelDesignSystem.TextSecondary.copy(alpha = 0.8f),
                )
                Text(
                    text = alert.timestamp,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelDesignSystem.TextPrimary.copy(alpha = 0.7f),
                )
            }
        }
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AlertHistoryScreenPreview() {
    TendeSeguro20Theme {
        AlertHistoryScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            onNavigateBack = {},
        )
    }
}
