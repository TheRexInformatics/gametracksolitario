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
import com.example.gametrack.GameViewModel
import com.example.gametrack.data.Game
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("PC") }
    var horas by remember { mutableStateOf("") }
    var calificacion by remember { mutableStateOf(5) }
    var notas by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf<String?>(null) }

    // Estados para búsqueda
    var isSearchingImage by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    var manualImageUrl by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val plataformas = listOf("PC", "PlayStation", "Xbox", "Nintendo Switch", "Mobile")
    val estados = listOf("Por jugar", "Jugando", "Completado", "Abandonado")
    var selectedEstado by remember { mutableStateOf("Por jugar") }

    // ⚠️ TEMPORAL: Desactivar búsqueda automática mientras escribes
    // En su lugar, usar un botón manual

    /*
    // COMENTADO TEMPORALMENTE - Causa crash
    LaunchedEffect(nombre) {
        if (nombre.length >= 3) {
            delay(1000)
            isSearchingImage = true
            searchMessage = "Buscando..."

            ApiGame.buscarCaratula(nombre) { url ->
                isSearchingImage = false
                if (url != null) {
                    imagenUrl = url
                    searchMessage = "✅ Imagen encontrada"
                } else {
                    searchMessage = "No se encontró imagen"
                }
            }
        }
    }
    */

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
                onValueChange = { nombre = it },
                label = { Text("Nombre del juego*") },
                placeholder = { Text("Ej: Minecraft, Fortnite...") },
                leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = "Juego") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para buscar imagen MANUALMENTE
            Button(
                onClick = {
                    if (nombre.length < 3) {
                        Toast.makeText(context, "Escribe al menos 3 caracteres", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSearchingImage = true
                    searchMessage = "Buscando imagen..."

                    ApiGame.buscarCaratula(nombre) { url ->
                        isSearchingImage = false

                        if (url != null) {
                            imagenUrl = url
                            searchMessage = "✅ Imagen encontrada automáticamente"
                            Toast.makeText(context, "Imagen encontrada!", Toast.LENGTH_SHORT).show()
                        } else {
                            searchMessage = "No se encontró imagen predefinida"
                            Toast.makeText(context, "No hay imagen predefinida para este nombre", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
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
                    Text("Buscar imagen automática")
                }
            }

            // Mensaje de estado
            if (searchMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    searchMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (searchMessage!!.startsWith("✅")) Color.Green
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. PLATAFORMA
            Text("Plataforma", style = MaterialTheme.typography.labelLarge)
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
            Text("Estado", style = MaterialTheme.typography.labelLarge)
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

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // 5. CALIFICACIÓN
            Text("Calificación: $calificacion/10", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = calificacion.toFloat(),
                onValueChange = { calificacion = it.toInt() },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonGreen,
                    activeTrackColor = NeonGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🖼️ Imagen del juego (Opcional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Vista previa
                    if (imagenUrl != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = imagenUrl),
                                contentDescription = "Imagen del juego",
                                modifier = Modifier
                                    .size(160.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Crop
                            )

                            Button(
                                onClick = { imagenUrl = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminar imagen")
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Sin imagen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Usa el botón 'Buscar imagen automática' arriba",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo para URL manual
                    OutlinedTextField(
                        value = manualImageUrl,
                        onValueChange = {
                            manualImageUrl = it
                            if (it.isNotBlank()) {
                                imagenUrl = it
                            }
                        },
                        label = { Text("URL de imagen manual (opcional)") },
                        placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = "URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                            val nuevoJuego = Game(
                                title = nombre,
                                platform = selectedPlatform,
                                status = selectedEstado,
                                rating = calificacion,
                                notes = if (notas.isNotBlank()) notas else null,
                                userId = viewModel.getCurrentUserId(),
                                imagenUrl = imagenUrl
                            )

                            viewModel.addGame(nuevoJuego)
                            Toast.makeText(context, "✅ Juego añadido", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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

            // Nota informativa
            Text(
                "* Campo requerido\n" +
                        "💡 Usa el botón azul para buscar imagen automática",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
