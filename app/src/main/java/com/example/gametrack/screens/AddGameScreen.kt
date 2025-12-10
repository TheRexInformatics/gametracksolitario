package com.example.gametrack.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.gametrack.BuildConfig
import com.example.gametrack.GameViewModel
import com.example.gametrack.data.Game
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("PC") }
    var horas by remember { mutableStateOf("") }
    var calificacion by remember { mutableStateOf(5f) } // Cambiado a Float
    var notas by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf<String?>(null) }

    // Estados para búsqueda
    var isSearchingImage by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    var manualImageUrl by remember { mutableStateOf("") }
    var lastSearched by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val plataformas = listOf("PC", "PlayStation", "Xbox", "Nintendo")
    val estados = listOf("Por jugar", "Jugando", "Completado", "Abandonado")
    var selectedEstado by remember { mutableStateOf("Por jugar") }

    // 🆕 BÚSQUEDA AUTOMÁTICA AL ESCRIBIR (con debounce)
    LaunchedEffect(nombre) {
        if (nombre.length >= 3 && nombre != lastSearched) {
            // Esperar 1 segundo después de dejar de escribir
            delay(1000)

            // Solo buscar si el nombre sigue siendo el mismo
            if (nombre.length >= 3 && nombre != lastSearched) {
                lastSearched = nombre
                isSearchingImage = true
                searchMessage = "🔍 Buscando imagen automáticamente..."

                Log.d("AddGameScreen", "🔄 Búsqueda automática para: '$nombre'")

                ApiGame.buscarCaratula(nombre) { url ->
                    isSearchingImage = false

                    if (url != null) {
                        imagenUrl = url
                        searchMessage = "✅ Imagen encontrada automáticamente"
                        Log.d("AddGameScreen", "✅ Imagen encontrada: $url")
                    } else {
                        searchMessage = "⚠️ No se encontró imagen predefinida"
                        Log.d("AddGameScreen", "⚠️ No se encontró imagen")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Añadir Juego") },
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. NOMBRE DEL JUEGO
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    // Limpiar mensaje si cambia el nombre
                    if (searchMessage != null && searchMessage!!.contains("encontrada")) {
                        searchMessage = null
                    }
                },
                label = { Text("Nombre del juego*") },
                placeholder = { Text("Ej: Minecraft, Fortnite...") },
                leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = "Juego") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (isSearchingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para buscar imagen MANUALMENTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (nombre.length < 3) {
                            Toast.makeText(context, "Escribe al menos 3 caracteres", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSearchingImage = true
                        searchMessage = "🔍 Buscando en bases de datos..."

                        ApiGame.buscarCaratula(nombre) { url ->
                            isSearchingImage = false

                            if (url != null) {
                                imagenUrl = url
                                searchMessage = "✅ Imagen encontrada automáticamente"
                                Toast.makeText(context, "¡Imagen encontrada!", Toast.LENGTH_SHORT).show()
                            } else {
                                searchMessage = "⚠️ No se encontró imagen para este nombre"
                                Toast.makeText(context, "No hay imagen predefinida", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    enabled = nombre.length >= 3 && !isSearchingImage
                ) {
                    if (isSearchingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscando...")
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar imagen")
                    }
                }

                // 🆕 BOTÓN PARA TEST IGDB
                Button(
                    onClick = {
                        scope.launch {
                            Toast.makeText(context, "🔌 Probando conexión IGDB...", Toast.LENGTH_SHORT).show()
                            ApiGame.probarConexionIGDB { conectado ->
                                val mensaje = if (conectado) "✅ IGDB conectado" else "❌ No hay conexión a IGDB"
                                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    enabled = !isSearchingImage
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = "Test IGDB")
                }
            }

            // Mensaje de estado
            if (searchMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            searchMessage!!.contains("✅") -> Color.Green.copy(alpha = 0.1f)
                            searchMessage!!.contains("⚠️") -> Color.Yellow.copy(alpha = 0.1f)
                            searchMessage!!.contains("🔍") -> Color.Blue.copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                searchMessage!!.contains("✅") -> Icons.Default.CheckCircle
                                searchMessage!!.contains("⚠️") -> Icons.Default.Warning
                                searchMessage!!.contains("🔍") -> Icons.Default.Search
                                else -> Icons.Default.Info
                            },
                            contentDescription = "Estado",
                            tint = when {
                                searchMessage!!.contains("✅") -> Color.Green
                                searchMessage!!.contains("⚠️") -> Color(0xFFFF9800)
                                searchMessage!!.contains("🔍") -> Color.Blue
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            searchMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                searchMessage!!.contains("✅") -> Color.Green
                                searchMessage!!.contains("⚠️") -> Color(0xFFFF9800)
                                searchMessage!!.contains("🔍") -> Color.Blue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. PLATAFORMA
            Text(
                "Plataforma",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                plataformas.forEach { plataforma ->
                    FilterChip(
                        selected = selectedPlatform == plataforma,
                        onClick = { selectedPlatform = plataforma },
                        label = { Text(plataforma) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                            selectedLabelColor = NeonGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. ESTADO
            Text(
                "Estado",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                estados.forEach { estado ->
                    FilterChip(
                        selected = selectedEstado == estado,
                        onClick = { selectedEstado = estado },
                        label = { Text(estado) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (estado) {
                                "Completado" -> Color.Green.copy(alpha = 0.2f)
                                "Jugando" -> Color.Blue.copy(alpha = 0.2f)
                                "Por jugar" -> Color.Gray.copy(alpha = 0.2f)
                                "Abandonado" -> Color.Red.copy(alpha = 0.2f)
                                else -> NeonGreen.copy(alpha = 0.2f)
                            },
                            selectedLabelColor = when (estado) {
                                "Completado" -> Color.Green
                                "Jugando" -> Color.Blue
                                "Por jugar" -> Color.Gray
                                "Abandonado" -> Color.Red
                                else -> NeonGreen
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. HORAS JUGADAS
            OutlinedTextField(
                value = horas,
                onValueChange = { horas = it },
                label = { Text("Horas jugadas (opcional)") },
                placeholder = { Text("Ej: 50") },
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = "Horas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. CALIFICACIÓN - NUEVO DISEÑO
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calificación",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${calificacion.toInt()}/10",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = calificacion,
                    onValueChange = { calificacion = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. NOTAS
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                leadingIcon = { Icon(Icons.Default.Note, contentDescription = "Notas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 7. SECCIÓN DE IMAGEN
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Imagen",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🖼️ Imagen del juego",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Vista previa
                    if (imagenUrl != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imagenUrl),
                                    contentDescription = "Imagen del juego",
                                    modifier = Modifier
                                        .size(180.dp)
                                        .padding(8.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { imagenUrl = null },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Eliminar")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            Toast.makeText(context, "🔄 Recargando imagen...", Toast.LENGTH_SHORT).show()
                                            isSearchingImage = true
                                            searchMessage = "🔍 Buscando de nuevo..."

                                            ApiGame.buscarCaratula(nombre) { url ->
                                                isSearchingImage = false
                                                if (url != null) {
                                                    imagenUrl = url
                                                    searchMessage = "✅ Nueva imagen encontrada"
                                                    Toast.makeText(context, "Imagen actualizada", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    searchMessage = "⚠️ No se encontró otra imagen"
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                                    modifier = Modifier.weight(1f),
                                    enabled = nombre.length >= 3 && !isSearchingImage
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buscar otra")
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.ImageNotSupported,
                                contentDescription = "Sin imagen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Usa el botón 'Buscar imagen' arriba",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "o introduce una URL manual abajo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo para URL manual
                    OutlinedTextField(
                        value = manualImageUrl,
                        onValueChange = {
                            manualImageUrl = it
                            if (it.isNotBlank() && it.startsWith("http")) {
                                imagenUrl = it
                                searchMessage = "✅ URL manual establecida"
                            }
                        },
                        label = { Text("URL de imagen manual (opcional)") },
                        placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = "URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "💡 La búsqueda automática usa IGDB y un cache local",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 8. BOTÓN GUARDAR
            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        Toast.makeText(context, "El nombre es requerido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        try {
                            val userId = viewModel.getCurrentUserId()
                            Log.d("AddGameScreen", "🆔 UserId obtenido: $userId")

                            if (userId <= 0) {
                                Toast.makeText(context, "❌ Error: No hay usuario logueado", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            val nuevoJuego = Game(
                                title = nombre,
                                platform = selectedPlatform,
                                status = selectedEstado,
                                rating = calificacion.toInt(), // Convertir Float a Int
                                notes = if (notas.isNotBlank()) notas else null,
                                userId = userId,
                                imagenUrl = imagenUrl
                            )

                            Log.d("AddGameScreen", "🎮 Creando juego: $nombre")
                            Log.d("AddGameScreen", "📊 Datos: platform=$selectedPlatform, status=$selectedEstado, rating=${calificacion.toInt()}, userId=$userId, imagen=${imagenUrl != null}")

                            viewModel.addGame(nuevoJuego)

                            Toast.makeText(context, "✅ Juego añadido correctamente", Toast.LENGTH_SHORT).show()

                            // Esperar un momento para que se guarde y luego navegar
                            delay(300)
                            navController.popBackStack()

                        } catch (e: Exception) {
                            Log.e("AddGameScreen", "💥 Error añadiendo juego: ${e.message}")
                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Juego")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}