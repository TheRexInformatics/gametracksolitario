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
import com.example.gametrack.data.GameDatabase
import com.example.gametrack.data.remote.api.ApiClient
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.*
import com.example.gametrack.BuildConfig
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
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
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("LoginScreen", "🚀 Pantalla Login cargada")
        try {
            ApiClient.init(context)
            ApiClient.logCurrentConfig()
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

        if (connectionStatus != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        connectionStatus!!.contains("✅") -> Color.Green.copy(alpha = 0.2f)
                        connectionStatus!!.contains("⚠️") -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        connectionStatus!!.contains("❌") -> Color.Red.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    connectionStatus!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        connectionStatus!!.contains("✅") -> Color.Green
                        connectionStatus!!.contains("⚠️") -> Color(0xFFFF9800)
                        connectionStatus!!.contains("❌") -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BOTÓN: LIMPIAR DATOS LOCALES (MANTENIDO)
            Button(
                onClick = {
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                val db = GameDatabase.getDatabase(context)
                                db.clearAllTables()
                                Log.d("LoginScreen", "🧹 Base de datos limpiada")
                            }
                            Toast.makeText(context, "Datos locales limpiados", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "❌ Error limpiando BD: ${e.message}")
                            Toast.makeText(context, "Error limpiando datos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading && !isTestingConnection
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Limpiar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("🧹 LIMPIAR DATOS LOCALES")
            }

            // BOTÓN: TEST CONEXIÓN DIRECTA (MANTENIDO)
            Button(
                onClick = {
                    scope.launch {
                        try {
                            Log.d("LoginScreen", "🚨 [TEST-URGENTE] Iniciando test OkHttp directo")
                            isTestingConnection = true
                            connectionStatus = "🚨 Probando conexión DIRECTA..."

                            val result = testDirectOkHttp()
                            Log.d("LoginScreen", "📊 [TEST-URGENTE] Resultado: $result")
                            connectionStatus = result
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "💥 [TEST-URGENTE] Error: ${e.message}")
                            connectionStatus = "💥 Error: ${e.message}"
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isTestingConnection = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading && !isTestingConnection
            ) {
                Icon(Icons.Default.Wifi, contentDescription = "Test Urgente")
                Spacer(modifier = Modifier.width(8.dp))
                Text("🚨 TEST CONEXIÓN DIRECTA")
            }

            // BOTÓN: TEST RETROFIT DIRECTO (MANTENIDO)
            Button(
                onClick = {
                    scope.launch {
                        try {
                            Log.d("LoginScreen", "🔧 [TEST-RETROFIT] Iniciando test Retrofit directo")
                            isTestingConnection = true
                            connectionStatus = "🔧 Probando Retrofit..."

                            val result = testDirectRetrofit()
                            Log.d("LoginScreen", "📊 [TEST-RETROFIT] Resultado: $result")
                            connectionStatus = result
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "💥 [TEST-RETROFIT] Error: ${e.message}")
                            connectionStatus = "💥 Error Retrofit: ${e.message}"
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isTestingConnection = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isLoading && !isTestingConnection
            ) {
                Icon(Icons.Default.Build, contentDescription = "Test Retrofit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔧 TEST RETROFIT DIRECTO")
            }
        }

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
                    enabled = !isLoading && !isTestingConnection
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
                            enabled = !isLoading && !isTestingConnection
                        ) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading && !isTestingConnection
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🚫 BOTÓN "PROBAR CON VIEWMODEL" - ELIMINADO (NO APARECE)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    vibrateError()
                    Toast.makeText(context, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    Log.d("LoginScreen", "🔐 [LOGIN] Iniciando proceso para: $username")
                    isLoading = true
                    connectionStatus = "🔐 Conectando..."

                    try {
                        Log.d("LoginScreen", "🟡 [LOGIN] Llamando a viewModel.loginUser()")
                        val loginExitoso = viewModel.loginUser(username, password)

                        if (loginExitoso) {
                            Log.d("LoginScreen", "✅ [LOGIN] Login EXITOSO")

                            when (val state = authState) {
                                is GameViewModel.AuthState.SuccessBackend -> {
                                    Toast.makeText(context, "✅ Backend: ¡Bienvenido $username!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                }
                                is GameViewModel.AuthState.SuccessLocal -> {
                                    Toast.makeText(context, "📱 Local: ¡Bienvenido $username!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                }
                                else -> {
                                    Toast.makeText(context, "¡Bienvenido $username!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                }
                            }
                        } else {
                            Log.d("LoginScreen", "❌ [LOGIN] Login FALLIDO")
                            vibrateError()

                            when (val state = authState) {
                                is GameViewModel.AuthState.Error -> {
                                    val errorMsg = state.message
                                    Log.d("LoginScreen", "🔍 [LOGIN] Error: $errorMsg")
                                    connectionStatus = "❌ Error: $errorMsg"
                                    Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                                is GameViewModel.AuthState.SuccessLocal -> {
                                    Log.d("LoginScreen", "🔍 [LOGIN] Usó modo LOCAL")
                                    connectionStatus = "📱 Usando modo local"
                                    Toast.makeText(context, "Modo local activado", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Log.d("LoginScreen", "🔍 [LOGIN] Estado desconocido")
                                    connectionStatus = "❌ Fallo desconocido"
                                    Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "💥 [LOGIN] ERROR CAPTURADO: ${e.message}")
                        vibrateError()
                        connectionStatus = "💥 Error: ${e.message}"
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        Log.d("LoginScreen", "🟢 [LOGIN] Proceso completado")
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isTestingConnection && username.isNotEmpty() && password.isNotEmpty()
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
                Text("🔐 INICIAR SESIÓN")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "¿No tienes cuenta? Regístrate aquí.",
            modifier = Modifier.clickable {
                if (!isLoading && !isTestingConnection) {
                    navController.navigate("signup")
                }
            },
            color = if (!isLoading && !isTestingConnection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (BuildConfig.DEBUG) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.05f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "🔍 INFORMACIÓN DEBUG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "📡 URL Backend: http://10.116.67.176:8080",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray
                    )

                    Text(
                        "🔐 Estado Auth: ${getAuthStateText(authState)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = getAuthStateColor(authState),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ========== FUNCIONES DE AYUDA ==========

private suspend fun testDirectOkHttp(): String = withContext(Dispatchers.IO) {
    try {
        Log.d("LoginScreen", "🔧 [OkHttp] Test directo...")
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("http://10.116.67.176:8080/api/auth/test")
            .build()

        Log.d("LoginScreen", "➡️ [OkHttp] Enviando a: http://10.116.67.176:8080/api/auth/test")
        val response = client.newCall(request).execute()

        val body = response.body?.string() ?: "Sin cuerpo"
        Log.d("LoginScreen", "⬅️ [OkHttp] Respuesta: ${response.code} - '$body'")

        return@withContext "✅ OkHttp: HTTP ${response.code} - '$body'"
    } catch (e: Exception) {
        Log.e("LoginScreen", "🔧 [OkHttp] Error: ${e.javaClass.simpleName}: ${e.message}")
        return@withContext "❌ OkHttp: ${e.javaClass.simpleName}: ${e.message}"
    }
}

private suspend fun testDirectRetrofit(): String = withContext(Dispatchers.IO) {
    try {
        Log.d("LoginScreen", "🔧 [Retrofit-SIMPLE] Iniciando test...")

        data class SimpleTestResponse(
            val status: String? = null,
            val message: String? = null,
            val timestamp: String? = null
        )

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("http://10.116.67.176:8080/api/auth/test")
            .build()

        Log.d("LoginScreen", "➡️ [Retrofit-SIMPLE] Enviando request...")
        val response = client.newCall(request).execute()

        val rawBody = response.body?.string() ?: "VACÍO"
        Log.d("LoginScreen", "⬅️ [Retrofit-SIMPLE] HTTP ${response.code}")
        Log.d("LoginScreen", "📦 [Retrofit-SIMPLE] RAW: '$rawBody'")

        if (!response.isSuccessful) {
            return@withContext "❌ Retrofit-SIMPLE: HTTP ${response.code} - '$rawBody'"
        }

        return@withContext try {
            val gson = Gson()
            val testResponse = gson.fromJson(rawBody, SimpleTestResponse::class.java)

            if (testResponse.message != null) {
                "✅ Retrofit-SIMPLE: '${testResponse.message}'"
            } else {
                "⚠️ Retrofit-SIMPLE: Respuesta JSON inesperada - '$rawBody'"
            }
        } catch (e: Exception) {
            "⚠️ Retrofit-SIMPLE: Texto plano - '$rawBody'"
        }

    } catch (e: java.net.ConnectException) {
        Log.e("LoginScreen", "❌ [Retrofit-SIMPLE] No hay conexión")
        "❌ Retrofit-SIMPLE: No hay conexión - Verifica IP/puerto"
    } catch (e: java.net.SocketTimeoutException) {
        Log.e("LoginScreen", "❌ [Retrofit-SIMPLE] Timeout")
        "❌ Retrofit-SIMPLE: Timeout - Servidor no responde"
    } catch (e: Exception) {
        Log.e("LoginScreen", "❌ [Retrofit-SIMPLE] Error: ${e.message}")
        "❌ Retrofit-SIMPLE: ${e.javaClass.simpleName} - ${e.message}"
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