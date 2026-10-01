package com.example.tendeseguro20

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme

// ============================================================================
// MODELO DE ESTADO REACTIVO (AppUiState)
// ============================================================================

/**
 * Define los 4 estados fundamentales reactivos de la aplicación:
 * - Loading: Carga inicial / consulta de microcontrolador ESP32
 * - Success: Datos de sensores recibidos y listos para renderizar
 * - Empty: Sin registros / primer uso sin elementos vinculados
 * - Error: Fallos de red o pérdida de comunicación con hardware IoT
 */
sealed interface AppUiState<out T> {
    data class Loading(
        val message: String = "Cargando sensores de la red ESP32...",
    ) : AppUiState<Nothing>

    data class Success<out T>(
        val data: T,
    ) : AppUiState<T>

    data class Empty(
        val title: String = "Sin Registros de Sensores",
        val description: String = "Aún no has vinculado tu módulo de sensor BX5 o no existen lecturas registradas.",
        val actionLabel: String = "Inicializar Primer Escaneo",
    ) : AppUiState<Nothing>

    data class Error(
        val userFriendlyMessage: String = "Pérdida de señal con el módulo ESP32. Verifica que tu dispositivo TendeSeguro esté encendido y conectado a la red local.",
        val canRetry: Boolean = true,
    ) : AppUiState<Nothing>
}

// ============================================================================
// CONTENEDOR REACTIVO PRINCIPAL: CrucialStateContainer
// ============================================================================

/**
 * Contenedor dinámico que conmuta suavemente entre estados evitando
 * pantallas en blanco o crashes de UI.
 */
@Composable
fun <T> CrucialStateContainer(
    state: AppUiState<T>,
    onRetry: () -> Unit,
    onInitializeFirstItem: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    Crossfade(
        targetState = state,
        animationSpec = tween(durationMillis = 350),
        label = "CrucialStateCrossfade",
        modifier = modifier,
    ) { currentState ->
        when (currentState) {
            is AppUiState.Loading -> CrucialLoadingView(message = currentState.message)
            is AppUiState.Success -> content(currentState.data)
            is AppUiState.Empty -> CrucialEmptyView(
                title = currentState.title,
                description = currentState.description,
                actionLabel = currentState.actionLabel,
                onActionClick = onInitializeFirstItem,
            )

            is AppUiState.Error -> CrucialErrorView(
                errorMessage = currentState.userFriendlyMessage,
                canRetry = currentState.canRetry,
                onRetryClick = onRetry,
            )
        }
    }
}

// ============================================================================
// 1. VISTA DE ESTADO VACÍO (EMPTY STATE)
// ============================================================================

@Composable
fun CrucialEmptyView(
    title: String,
    description: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFAF8F5),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFFD0E2FF), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD0E2FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "📊", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color(0xFF7F8C8D),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón para inicializar / crear primer elemento
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A90E2),
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = actionLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. VISTA DE ESTADO DE CARGA (LOADING STATE)
// ============================================================================

@Composable
fun CrucialLoadingView(
    message: String,
    modifier: Modifier = Modifier,
) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Spinner indicador de progreso en parte superior
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFD0E2FF), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FE)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF4A90E2),
                    strokeWidth = 2.5.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50),
                )
            }
        }

        // Skeletons Animados
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EFEA)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(shimmerBrush),
            )
        }

        repeat(2) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EFEA)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(shimmerBrush),
                )
            }
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ShimmerTranslation",
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0).copy(alpha = 0.6f),
            Color(0xFFF8FAFC).copy(alpha = 0.9f),
            Color(0xFFE2E8F0).copy(alpha = 0.6f),
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim),
    )
}

// ============================================================================
// 3. VISTA DE ESTADO DE ERROR (ERROR STATE)
// ============================================================================

@Composable
fun CrucialErrorView(
    errorMessage: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFAF8F5),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFFFFC4B8), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0ED)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC4B8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF476F),
                            modifier = Modifier.size(40.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Error de Conexión IoT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF7F8C8D),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                    )

                    if (canRetry) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onRetryClick,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF476F),
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reconectar / Reintentar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
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
fun CrucialStateContainerPreview() {
    TendeSeguro20Theme {
        CrucialStateContainer<String>(
            state = AppUiState.Empty(),
            onRetry = {},
            onInitializeFirstItem = {},
        ) { data ->
            Text(text = data)
        }
    }
}
