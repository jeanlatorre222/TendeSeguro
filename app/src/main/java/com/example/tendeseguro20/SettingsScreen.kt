package com.example.tendeseguro20

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme

// ============================================================================
// PALETA PASTEL DE CONFIGURACIÓN
// ============================================================================

private object SettingsPastel {
    val Background = Color(0xFFFAF8F5)
    val TextPrimary = Color(0xFF2C3E50)
    val TextSecondary = Color(0xFF7F8C8D)

    val CardBackground = Color(0xFFEBF3FE)
    val CardBorder = Color(0xFFD0E2FF)
    val AccentBlue = Color(0xFF4A90E2)

    val CardBackgroundGreen = Color(0xFFEBF8F2)
    val CardBorderGreen = Color(0xFFC7EFE0)
    val AccentGreen = Color(0xFF7BCD9B)

    val LogoutBackground = Color(0xFFFFF0ED)
    val LogoutBorder = Color(0xFFFFC4B8)
    val LogoutRed = Color(0xFFEF476F)
}

// ============================================================================
// COMPOSABLE PRINCIPAL: SettingsScreen
// ============================================================================

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val secureTokenManager = remember { SecureTokenManager(context) }
    val userEmail = remember { secureTokenManager.getUserEmail() ?: "usuario@tendeseguro.com" }

    // Estados de preferencias de notificaciones
    var rainAlertsEnabled by remember { mutableStateOf(true) }
    var sensorBx5AlertsEnabled by remember { mutableStateOf(true) }
    var dailySummaryEnabled by remember { mutableStateOf(false) }

    // Estados de diálogos
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SettingsPastel.Background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Header con botón Atrás y Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar al Dashboard",
                        tint = SettingsPastel.TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Perfil y Configuración",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SettingsPastel.TextPrimary,
                )
            }

            // TARJETA DE PERFIL DE USUARIO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, SettingsPastel.CardBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SettingsPastel.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SettingsPastel.CardBorder),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SettingsPastel.AccentBlue,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Usuario TendeSeguro",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SettingsPastel.TextPrimary,
                        )
                        Text(
                            text = userEmail,
                            fontSize = 12.sp,
                            color = SettingsPastel.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SettingsPastel.AccentGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Módulo BX5 Conectado",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SettingsPastel.AccentGreen,
                            )
                        }
                    }
                }
            }

            // SECCIÓN 1: PREFERENCIAS DE NOTIFICACIONES
            Text(
                text = "Notificaciones y Alertas",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SettingsPastel.TextPrimary,
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, SettingsPastel.CardBorderGreen, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SettingsPastel.CardBackgroundGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Switch 1: Alertas de lluvia
                    SettingSwitchItem(
                        title = "Alertas de Lluvia Inminente",
                        subtitle = "Avisos instantáneos cuando la humedad supere el 80%",
                        checked = rainAlertsEnabled,
                        onCheckedChange = { rainAlertsEnabled = it },
                    )

                    HorizontalDivider(
                        color = SettingsPastel.CardBorderGreen,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )

                    // Switch 2: Sensor BX5
                    SettingSwitchItem(
                        title = "Alertas de Agua Sensor BX5",
                        subtitle = "Notificar acumulación de agua en la plataforma",
                        checked = sensorBx5AlertsEnabled,
                        onCheckedChange = { sensorBx5AlertsEnabled = it },
                    )

                    HorizontalDivider(
                        color = SettingsPastel.CardBorderGreen,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )

                    // Switch 3: Resumen Diario
                    SettingSwitchItem(
                        title = "Resumen Diario de Clima",
                        subtitle = "Recibir reporte matutino de temperatura y humedad",
                        checked = dailySummaryEnabled,
                        onCheckedChange = { dailySummaryEnabled = it },
                    )
                }
            }

            // SECCIÓN 2: INFORMACIÓN LEGAL Y SOPORTE
            Text(
                text = "Información Legal y Sistema",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SettingsPastel.TextPrimary,
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, SettingsPastel.CardBorder, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SettingsPastel.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Item Términos de Servicio
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SettingsPastel.AccentBlue,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Términos de Servicio y Privacidad",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SettingsPastel.TextPrimary,
                            )
                        }
                        Text(text = "❯", fontSize = 14.sp, color = SettingsPastel.TextSecondary)
                    }

                    HorizontalDivider(
                        color = SettingsPastel.CardBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )

                    // Item Versión
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Versión de la App",
                            fontSize = 13.sp,
                            color = SettingsPastel.TextSecondary,
                        )
                        Text(
                            text = "v2.0 (Build 102)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SettingsPastel.TextPrimary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECCIÓN 3: BOTÓN CLARO PARA CERRAR SESIÓN
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, SettingsPastel.LogoutBorder, RoundedCornerShape(22.dp))
                    .clickable { showLogoutConfirmDialog = true },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SettingsPastel.LogoutBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "🚪", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SettingsPastel.LogoutRed,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // DIÁLOGO TÉRMINOS DE SERVICIO
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Términos de Servicio y Privacidad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SettingsPastel.TextPrimary,
                )
            },
            text = {
                Text(
                    text = "TendeSeguro 2.0 utiliza datos cifrados de sensores de temperatura, humedad y agua (módulo BX5) para enviar alertas preventivas en tiempo real. Los datos son almacenados localmente de forma segura mediante claves cifradas KeyStore y no son compartidos con terceros.",
                    fontSize = 13.sp,
                    color = SettingsPastel.TextSecondary,
                    lineHeight = 18.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsPastel.AccentBlue),
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SettingsPastel.Background,
        )
    }

    // DIÁLOGO CONFIRMACIÓN CIERRE DE SESIÓN
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Confirmar Cierre de Sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SettingsPastel.TextPrimary,
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas cerrar sesión? Tus tokens cifrados de acceso serán removidos de forma segura del dispositivo.",
                    fontSize = 13.sp,
                    color = SettingsPastel.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsPastel.LogoutRed),
                ) {
                    Text("Sí, Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SettingsPastel.Background,
        )
    }
}

// ============================================================================
// ITEM SWITCH REUTILIZABLE
// ============================================================================

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SettingsPastel.TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SettingsPastel.TextSecondary,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsPastel.AccentGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SettingsPastel.TextSecondary.copy(alpha = 0.3f),
            ),
        )
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    TendeSeguro20Theme {
        SettingsScreen(
            onNavigateBack = {},
            onLogoutClick = {},
        )
    }
}
