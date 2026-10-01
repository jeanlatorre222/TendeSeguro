package com.example.tendeseguro20

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.tendeseguro20.ui.theme.TendeSeguro20Theme

// ============================================================================
// PALETA PASTEL DE AUTENTICACIÓN
// ============================================================================

private object AuthPastel {
    val Background = Color(0xFFFBF9F5)
    val TextPrimary = Color(0xFF2C3E50)
    val TextSecondary = Color(0xFF7F8C8D)
    val CardBackground = Color(0xFFEBF3FE)
    val CardBorder = Color(0xFFD0E2FF)
    val AccentBlue = Color(0xFF4A90E2)
    val AccentGreen = Color(0xFF7BCD9B)
    val InputBackground = Color(0xFFFFFFFF)
    val InputBorder = Color(0xFFCBD5E1)
}

enum class AuthMode {
    LOGIN,
    REGISTER
}

// ============================================================================
// COMPOSABLE PRINCIPAL: AuthScreen
// ============================================================================

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Estados de Formulario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibilidad de contraseña
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Estado de mensajes de error/éxito
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Gestión inteligente de focos del teclado
    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    val secureTokenManager = remember { SecureTokenManager(context) }
    val isBiometricAvailable = remember { BiometricAuthManager.canAuthenticate(context) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AuthPastel.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(), // Evita que el teclado tape los inputs al escribir
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AuthPastel.CardBorder),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TendeSeguro 2.0",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AuthPastel.TextPrimary
            )

            Text(
                text = if (authMode == AuthMode.LOGIN) "Bienvenido de nuevo a tu monitoreo IoT" else "Crea tu cuenta para proteger tu hogar",
                fontSize = 13.sp,
                color = AuthPastel.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Pestañas (Login / Registro)
            AuthTabSelector(
                selectedMode = authMode,
                onModeSelected = { mode ->
                    authMode = mode
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta con Campos de Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, AuthPastel.CardBorder, RoundedCornerShape(24.dp))
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AuthPastel.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Campo Nombre Completo (Solo Registro)
                    AnimatedVisibility(visible = authMode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre Completo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AuthPastel.TextSecondary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { emailFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Campo Correo Electrónico
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = AuthPastel.TextSecondary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                    )

                    // Campo Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AuthPastel.TextSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Text(
                                    text = if (isPasswordVisible) "👁️" else "🙈",
                                    fontSize = 16.sp
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (authMode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { confirmPasswordFocusRequester.requestFocus() },
                            onDone = {
                                focusManager.clearFocus()
                                performAuth(
                                    authMode = authMode,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    secureTokenManager = secureTokenManager,
                                    onError = { errorMessage = it },
                                    onSuccess = onAuthSuccess
                                )
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester)
                    )

                    // Campo Confirmar Contraseña (Solo Registro)
                    AnimatedVisibility(visible = authMode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AuthPastel.TextSecondary
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    performAuth(
                                        authMode = authMode,
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        secureTokenManager = secureTokenManager,
                                        onError = { errorMessage = it },
                                        onSuccess = onAuthSuccess
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(confirmPasswordFocusRequester)
                        )
                    }

                    // Mensaje de Error
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = Color(0xFFE57373),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón Principal de Acción (Iniciar Sesión / Registrarse)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            performAuth(
                                authMode = authMode,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                secureTokenManager = secureTokenManager,
                                onError = { errorMessage = it },
                                onSuccess = onAuthSuccess
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuthPastel.AccentBlue,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "Iniciar Sesión" else "Crear Cuenta",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sección de Autenticación Biométrica (Solo en Login si está disponible)
            if (authMode == AuthMode.LOGIN && isBiometricAvailable) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AuthPastel.AccentGreen.copy(alpha = 0.15f))
                        .border(1.dp, AuthPastel.AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .clickable {
                            if (context is FragmentActivity) {
                                BiometricAuthManager.showBiometricPrompt(
                                    activity = context,
                                    onSuccess = {
                                        secureTokenManager.saveAuthToken("biometric_secure_token_sample", email = email.ifEmpty { "usuario@tendeseguro.com" })
                                        onAuthSuccess()
                                    },
                                    onError = { err -> errorMessage = err }
                                )
                            }
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "👆", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ingresar con Huella o Rostro",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuthPastel.TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ============================================================================
// COMPONENTES SECUNDARIOS Y CONFIGURACIÓN DE ESTILOS
// ============================================================================

@Composable
private fun AuthTabSelector(
    selectedMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, AuthPastel.CardBorder, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selectedMode == AuthMode.LOGIN) AuthPastel.AccentBlue else Color.Transparent)
                .clickable { onModeSelected(AuthMode.LOGIN) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Iniciar Sesión",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedMode == AuthMode.LOGIN) Color.White else AuthPastel.TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selectedMode == AuthMode.REGISTER) AuthPastel.AccentBlue else Color.Transparent)
                .clickable { onModeSelected(AuthMode.REGISTER) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Registrarse",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedMode == AuthMode.REGISTER) Color.White else AuthPastel.TextSecondary
            )
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AuthPastel.InputBackground,
    unfocusedContainerColor = AuthPastel.InputBackground,
    focusedBorderColor = AuthPastel.AccentBlue,
    unfocusedBorderColor = AuthPastel.InputBorder,
    focusedLabelColor = AuthPastel.AccentBlue,
    unfocusedLabelColor = AuthPastel.TextSecondary
)

private fun performAuth(
    authMode: AuthMode,
    email: String,
    password: String,
    confirmPassword: String,
    secureTokenManager: SecureTokenManager,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    if (email.isBlank() || password.isBlank()) {
        onError("Por favor, completa todos los campos marcados.")
        return
    }

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("Ingresa un correo electrónico válido.")
        return
    }

    if (password.length < 6) {
        onError("La contraseña debe tener al menos 6 caracteres.")
        return
    }

    if (authMode == AuthMode.REGISTER && password != confirmPassword) {
        onError("Las contraseñas no coinciden.")
        return
    }

    // Almacenamiento seguro del token generado de autenticación
    secureTokenManager.saveAuthToken(
        token = "jwt_token_sample_${System.currentTimeMillis()}",
        email = email
    )

    onSuccess()
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    TendeSeguro20Theme {
        AuthScreen(onAuthSuccess = {})
    }
}
