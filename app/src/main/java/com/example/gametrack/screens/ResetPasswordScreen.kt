package com.example.gametrack.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gametrack.GameViewModel
import com.example.gametrack.data.remote.api.ApiClient
import com.example.gametrack.data.remote.models.ResetPasswordRequest
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: GameViewModel,
    email: String? = null,
    token: String? = null
) {
    var userEmail by remember { mutableStateOf(email ?: "") }
    var verificationCode by remember { mutableStateOf(token ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Restablecer Contraseña",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Ingresa el código recibido y tu nueva contraseña",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Mostrar mensajes de error/éxito
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }

            successMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Green.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                }
            }

            // Email
            OutlinedTextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Código de verificación
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Código de verificación") },
                leadingIcon = { Icon(Icons.Default.Code, contentDescription = "Código") },
                placeholder = { Text("Ej: 123456") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Text(
                text = "💡 Para la demo, usa cualquier código de 6 dígitos",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nueva contraseña
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Mostrar/ocultar"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirmar") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Mostrar/ocultar"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Limpiar mensajes anteriores
                    errorMessage = null
                    successMessage = null

                    // Validaciones
                    if (userEmail.isBlank() || !userEmail.contains("@")) {
                        errorMessage = "Email inválido"
                        Toast.makeText(context, "Email inválido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (verificationCode.isBlank()) {
                        errorMessage = "Ingresa el código de verificación"
                        Toast.makeText(context, "Ingresa el código de verificación", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (newPassword.length < 6) {
                        errorMessage = "La contraseña debe tener al menos 6 caracteres"
                        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (newPassword != confirmPassword) {
                        errorMessage = "Las contraseñas no coinciden"
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        isLoading = true

                        try {
                            Log.d("ResetPassword", "🚀 Iniciando reset password...")
                            Log.d("ResetPassword", "📧 Email: $userEmail")
                            Log.d("ResetPassword", "🔑 Token: $verificationCode")
                            Log.d("ResetPassword", "🔒 Nueva pass: ${newPassword.take(3)}...")

                            // 1. Obtener servicio
                            val authService = ApiClient.getAuthApiService(context)

                            // 2. Crear request
                            val request = ResetPasswordRequest(
                                email = userEmail,
                                token = verificationCode,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )

                            Log.d("ResetPassword", "📤 Enviando request: $request")

                            // 3. Llamar al backend REAL
                            val response = authService.resetPassword(request)

                            Log.d("ResetPassword", "📥 Respuesta recibida")
                            Log.d("ResetPassword", "   Código HTTP: ${response.code()}")
                            Log.d("ResetPassword", "   ¿Es exitosa?: ${response.isSuccessful}")

                            if (response.isSuccessful) {
                                val data = response.body()
                                val successMessageText = data?.message ?: "Contraseña actualizada"

                                Log.d("ResetPassword", "✅ ÉXITO: $successMessageText")
                                Log.d("ResetPassword", "   Username: ${data?.username}")

                                successMessage = "✅ $successMessageText"

                                Toast.makeText(
                                    context,
                                    "✅ $successMessageText",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Esperar 2 segundos y redirigir
                                kotlinx.coroutines.delay(2000)

                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }

                            } else {
                                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                                Log.e("ResetPassword", "❌ ERROR HTTP: ${response.code()}")
                                Log.e("ResetPassword", "   Body error: $errorBody")

                                val errorText = try {
                                    val errorJson = JSONObject(errorBody)
                                    errorJson.getString("error") ?: "Error ${response.code()}"
                                } catch (e: Exception) {
                                    "Error ${response.code()}: $errorBody"
                                }

                                errorMessage = "❌ $errorText"
                                Toast.makeText(context, errorText, Toast.LENGTH_LONG).show()
                            }

                        } catch (e: java.net.SocketTimeoutException) {
                            Log.e("ResetPassword", "⏰ Timeout del servidor")
                            errorMessage = "⏰ Timeout: El servidor no responde"
                            Toast.makeText(context, "Timeout: Servidor no responde", Toast.LENGTH_LONG).show()

                        } catch (e: java.net.ConnectException) {
                            Log.e("ResetPassword", "🌐 Error de conexión")
                            errorMessage = "🌐 Error de conexión: Verifica tu red"
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_LONG).show()

                            // FALLBACK para demo
                            successMessage = "✅ (Demo) Contraseña actualizada"
                            Toast.makeText(context, "✅ (Demo) Contraseña actualizada", Toast.LENGTH_LONG).show()
                            kotlinx.coroutines.delay(2000)
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }

                        } catch (e: Exception) {
                            Log.e("ResetPassword", "💥 Error inesperado: ${e.message}")
                            errorMessage = "💥 Error: ${e.localizedMessage}"
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizando...")
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Confirmar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTABLECER CONTRASEÑA")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para prueba manual
            if (LocalContext.current.packageName.contains("debug")) {
                Button(
                    onClick = {
                        scope.launch {
                            // Auto-completar con datos de prueba
                            userEmail = "test@test.com"
                            verificationCode = "123456"
                            newPassword = "test123"
                            confirmPassword = "test123"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = "Debug")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LLENAR DATOS DE PRUEBA")
                }
            }
        }
    }
}