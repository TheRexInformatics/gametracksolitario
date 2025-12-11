package com.example.gametrack.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gametrack.GameViewModel
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var receivedToken by remember { mutableStateOf<String?>(null) }
    var showTokenDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recuperar Contraseña") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = "Recuperar contraseña",
                modifier = Modifier.size(80.dp),
                tint = NeonGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa tu correo electrónico y te enviaremos un código de verificación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("ejemplo@correo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || !email.contains("@")) {
                        Toast.makeText(context, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            kotlinx.coroutines.delay(1000)

                            receivedToken = "123456"
                            showTokenDialog = true

                            Toast.makeText(
                                context,
                                "✅ Código enviado (modo demo)",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviando...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENVIAR CÓDIGO")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }

    if (showTokenDialog && receivedToken != null) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            title = { Text("🔑 Código de Verificación") },
            text = {
                Column {
                    Text("Para la DEMO, usa este código:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        receivedToken!!,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonGreen,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "En la siguiente pantalla, ingresa:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("• Email: $email")
                    Text("• Código: ${receivedToken}")
                    Text("• Nueva contraseña")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTokenDialog = false
                        navController.navigate("resetPassword/$email/$receivedToken")
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}