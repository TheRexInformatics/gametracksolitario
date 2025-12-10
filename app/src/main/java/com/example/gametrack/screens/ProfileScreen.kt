package com.example.gametrack.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.gametrack.GameViewModel
import com.example.gametrack.R
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

// HOLDER PARA PERSISTIR LA IMAGEN
object ProfileImageHolder {
    var savedImageUri: Uri? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🆕 OBTENER DATOS DEL VIEWMODEL
    val username = viewModel.getCurrentUsername()
    val email = viewModel.getCurrentEmail()

    // Estados para imagen de perfil - CARGA DESDE HOLDER
    var profileImageUri by remember {
        mutableStateOf(ProfileImageHolder.savedImageUri)
    }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // FORZAR carga de datos del usuario y juegos
    LaunchedEffect(Unit) {
        viewModel.loadGamesForCurrentUser()
    }

    val games by viewModel.games.collectAsState()

    // ========== FUNCIÓN CLAVE QUE SÍ FUNCIONA ==========
    fun createImageFileUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        val authority = "${context.packageName}.provider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    // ========== LAUNCHERS ==========

    // 1. Launcher para GALERÍA
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            ProfileImageHolder.savedImageUri = it
            Toast.makeText(context, "✅ Imagen actualizada desde galería", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Launcher para CÁMARA
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            profileImageUri = tempCameraUri
            ProfileImageHolder.savedImageUri = tempCameraUri
            Toast.makeText(context, "✅ Foto tomada exitosamente", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Launcher para PERMISO DE CÁMARA
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createImageFileUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(context, "❌ Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // ========== FUNCIONES SIMPLES ==========

    fun openCamera() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TARJETA DE PERFIL
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Image(
                            painter = if (profileImageUri != null) {
                                rememberAsyncImagePainter(model = profileImageUri)
                            } else {
                                painterResource(id = R.drawable.ic_default_avatar)
                            },
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable { showImageSourceDialog = true },
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { showImageSourceDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(NeonGreen, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // DIÁLOGO PARA ELEGIR FUENTE
                    if (showImageSourceDialog) {
                        AlertDialog(
                            onDismissRequest = { showImageSourceDialog = false },
                            title = { Text("Cambiar foto de perfil") },
                            text = { Text("Selecciona la fuente de la imagen") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showImageSourceDialog = false
                                        openCamera()
                                    }
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tomar foto")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = {
                                        showImageSourceDialog = false
                                        openGallery()
                                    }
                                ) {
                                    Icon(Icons.Default.Photo, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Elegir de galería")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (email.isNotEmpty()) email else "No disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // BOTÓN PARA GUARDAR IMAGEN
                        if (profileImageUri != null) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        Toast.makeText(
                                            context,
                                            "💾 Imagen guardada en perfil",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Guardar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar como foto de perfil")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Cuenta local",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Colección: ${games.size} juegos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // TARJETA DE ESTADÍSTICAS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Estadísticas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(
                            title = "Total Juegos",
                            value = games.size.toString(),
                            icon = Icons.Default.Games,
                            color = NeonGreen
                        )

                        val completedGames = games.count { it.status.equals("completado", ignoreCase = true) }
                        StatCard(
                            title = "Completados",
                            value = completedGames.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color.Green
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val averageRating = if (games.isNotEmpty()) {
                            games.sumOf { it.rating } / games.size
                        } else {
                            0
                        }

                        StatCard(
                            title = "Calificación Prom.",
                            value = if (averageRating > 0) "$averageRating/10" else "-",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFD700)
                        )

                        val favoritePlatform = if (games.isNotEmpty()) {
                            games.groupBy { it.platform }
                                .maxByOrNull { it.value.size }
                                ?.key ?: "Ninguna"
                        } else {
                            "Ninguna"
                        }

                        StatCard(
                            title = "Plataforma Favorita",
                            value = favoritePlatform,
                            icon = Icons.Default.Devices,
                            color = Color.Blue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN CERRAR SESIÓN
            Button(
                onClick = {
                    viewModel.logout()
                    ProfileImageHolder.savedImageUri = null
                    Toast.makeText(context, "👋 Sesión cerrada", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}