package com.example.gametrack.screens

import android.content.Context
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gametrack.GameViewModel
import com.example.gametrack.R
import com.example.gametrack.data.remote.api.ApiClient
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.*
import com.example.gametrack.BuildConfig
import android.util.Log
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("LoginScreen", "🚀 Pantalla Login cargada")
        try {
            ApiClient.init(context)
        } catch (e: Exception) {
            Log.e("LoginScreen", "❌ Error inicializando ApiClient: ${e.message}")
        }
    }

    fun vibrateError() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(100)
            }
        } catch (e: Exception) {
            Log.e("LoginScreen", "Error en vibración: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_gametrack),
            contentDescription = "Logo de GameTrack",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            "GameTrack",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            ),
            color = NeonGreen,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    placeholder = { Text("Ej: jugador123") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    placeholder = { Text("Mínimo 6 caracteres") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Contraseña",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ocultar" else "Mostrar"

                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLoading
                        ) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    vibrateError()
                    Toast.makeText(context, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    Log.d("LoginScreen", "🔐 Iniciando login para: $username")
                    isLoading = true

                    try {
                        val loginExitoso = viewModel.loginUser(username, password)

                        if (loginExitoso) {
                            Log.d("LoginScreen", "✅ Login exitoso")

                            when (val state = authState) {
                                is GameViewModel.AuthState.SuccessBackend -> {
                                    Toast.makeText(context, "✅ ¡Bienvenido $username!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                is GameViewModel.AuthState.SuccessLocal -> {
                                    Toast.makeText(context, "📱 ¡Bienvenido $username!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                else -> {
                                    Toast.makeText(context, "¡Bienvenido $username!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        } else {
                            Log.d("LoginScreen", "❌ Login fallido")
                            vibrateError()
                            Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "💥 Error en login: ${e.message}")
                        vibrateError()
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conectando...")
            } else {
                Icon(Icons.Default.Login, contentDescription = "Iniciar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("INICIAR SESIÓN")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ENLACE A REGISTRO
        Text(
            "¿No tienes cuenta? Regístrate aquí.",
            modifier = Modifier.clickable {
                if (!isLoading) {
                    navController.navigate("signup")
                }
            },
            color = if (!isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ¡NUEVO! ENLACE A RECUPERACIÓN DE CONTRASEÑA
        Text(
            "¿Olvidaste tu contraseña?",
            modifier = Modifier.clickable {
                if (!isLoading) {
                    navController.navigate("forgotPassword")
                }
            },
            color = if (!isLoading) Color.Blue else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔍 SOLO mostrar debug info en modo DEBUG
        if (BuildConfig.DEBUG) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        "🔍 MODO DEBUG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Estado Auth: ${getAuthStateText(authState)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = getAuthStateColor(authState),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun getAuthStateText(state: GameViewModel.AuthState): String {
    return when (state) {
        is GameViewModel.AuthState.Idle -> "Inactivo"
        is GameViewModel.AuthState.Loading -> "Cargando..."
        is GameViewModel.AuthState.SuccessBackend -> "✅ Backend"
        is GameViewModel.AuthState.SuccessLocal -> "📱 Local"
        is GameViewModel.AuthState.Error -> "❌ Error"
        else -> "Desconocido"
    }
}

private fun getAuthStateColor(state: GameViewModel.AuthState): Color {
    return when (state) {
        is GameViewModel.AuthState.SuccessBackend -> Color.Green
        is GameViewModel.AuthState.SuccessLocal -> Color(0xFFFF9800)
        is GameViewModel.AuthState.Error -> Color.Red
        is GameViewModel.AuthState.Loading -> Color.Blue
        else -> Color.Gray
    }
}