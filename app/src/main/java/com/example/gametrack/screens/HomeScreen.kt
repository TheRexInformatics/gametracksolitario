package com.example.gametrack.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.gametrack.BuildConfig
import com.example.gametrack.GameViewModel
import com.example.gametrack.R
import com.example.gametrack.data.Game
import com.example.gametrack.ui.theme.NeonGreen
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val games by viewModel.games.collectAsState()
    val currentUsername by remember { derivedStateOf { viewModel.getCurrentUsername() } }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Cargar juegos cuando se monte la pantalla
    LaunchedEffect(Unit) {
        android.util.Log.d("HomeScreen", "🏠 HomeScreen montada")
        android.util.Log.d("HomeScreen", "🆔 UserId actual: ${viewModel.getCurrentUserId()}")
        android.util.Log.d("HomeScreen", "👤 Usuario actual: $currentUsername")

        viewModel.loadGamesForCurrentUser()
    }

    // También recargar cuando la pantalla gane foco
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.route == "home") {
                android.util.Log.d("HomeScreen", "🔄 Recargando juegos al volver a Home")
                viewModel.loadGamesForCurrentUser()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mis Juegos",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }

                    // BOTÓN DEBUG
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = {
                            val debugInfo = viewModel.debugInfo()
                            Toast.makeText(context, debugInfo, Toast.LENGTH_LONG).show()
                            android.util.Log.d("HomeScreen", debugInfo)
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "Debug Info")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addGame") },
                containerColor = NeonGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir juego")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Encabezado con usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_default_avatar),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "¡Hola, $currentUsername!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tienes ${games.size} juegos en tu colección",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Mostrar errores
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Lista de juegos
            if (games.isEmpty()) {
                // Pantalla vacía
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_empty_games),
                        contentDescription = "Sin juegos",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No tienes juegos aún",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Presiona el botón + para añadir tu primer juego",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("addGame") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.width(200.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir Juego")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN PARA FORZAR RECARGA
                    Button(
                        onClick = {
                            viewModel.loadGamesForCurrentUser()
                            Toast.makeText(context, "🔄 Recargando juegos...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier.width(200.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recargar Lista")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { game ->
                        GameCardListStyle(
                            game = game,
                            onClick = {
                                // FUTURO: Navegar a pantalla de detalles/edición
                                Toast.makeText(context, "Seleccionado: ${game.title}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                // ELIMINAR JUEGO CON CONFIRMACIÓN
                                viewModel.deleteGame(game)
                                Toast.makeText(context, "✅ \"${game.title}\" eliminado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = NeonGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Cargando juegos...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardListStyle(
    game: Game,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImageFullscreen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🖼️ IMAGEN A LA IZQUIERDA
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (game.imagenUrl != null) {
                            showImageFullscreen = true
                        }
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (game.imagenUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = game.imagenUrl,
                            error = painterResource(id = R.drawable.ic_empty_games)
                        ),
                        contentDescription = "Imagen de ${game.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Indicador de que se puede hacer tap
                    if (game.imagenUrl != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                    }
                } else {
                    // Mostrar ícono si no hay imagen
                    Image(
                        painter = painterResource(id = R.drawable.ic_empty_games),
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(32.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 📄 INFORMACIÓN A LA DERECHA
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Fila: Título y botón eliminar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar juego",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fila: Plataforma y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge de plataforma
                    Surface(
                        color = NeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = game.platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Badge de estado
                    Surface(
                        color = when (game.status.lowercase()) {
                            "completado" -> Color.Green.copy(alpha = 0.2f)
                            "jugando" -> Color.Blue.copy(alpha = 0.2f)
                            "por jugar" -> Color.Gray.copy(alpha = 0.2f)
                            "abandonado" -> Color.Red.copy(alpha = 0.2f)
                            else -> Color.Gray.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                when (game.status.lowercase()) {
                                    "completado" -> Icons.Default.CheckCircle
                                    "jugando" -> Icons.Default.PlayCircle
                                    "por jugar" -> Icons.Default.Schedule
                                    "abandonado" -> Icons.Default.Cancel
                                    else -> Icons.Default.Help
                                },
                                contentDescription = "Estado",
                                tint = when (game.status.lowercase()) {
                                    "completado" -> Color.Green
                                    "jugando" -> Color.Blue
                                    "por jugar" -> Color.Gray
                                    "abandonado" -> Color.Red
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = game.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (game.status.lowercase()) {
                                    "completado" -> Color.Green
                                    "jugando" -> Color.Blue
                                    "por jugar" -> Color.Gray
                                    "abandonado" -> Color.Red
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calificación
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${game.rating}/10",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    // Mostrar horas si están en las notas
                    game.notes?.let { notas ->
                        val horasRegex = """(\d+)\s*(horas?|h)""".toRegex(RegexOption.IGNORE_CASE)
                        horasRegex.find(notas)?.let { match ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = "Horas jugadas",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = match.value,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Notas (si existen y no son solo horas)
                game.notes?.takeIf { notas ->
                    notas.isNotBlank() &&
                            notas.length > 3 &&
                            !"""^\d+\s*(horas?|h)$""".toRegex(RegexOption.IGNORE_CASE).matches(notas.trim())
                }?.let { notas ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notas,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN PARA ELIMINAR
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar juego") },
            text = { Text("¿Estás seguro de que quieres eliminar \"${game.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🖼️ DIÁLOGO PARA VER IMAGEN EN PANTALLA COMPLETA
    if (showImageFullscreen && game.imagenUrl != null) {
        Dialog(
            onDismissRequest = { showImageFullscreen = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = game.imagenUrl,
                                error = painterResource(id = R.drawable.ic_empty_games)
                            ),
                            contentDescription = "Imagen completa de ${game.title}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Botón para cerrar
                        IconButton(
                            onClick = { showImageFullscreen = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }

                    // Pie de foto
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${game.platform} • ${game.status} • ${game.rating}/10",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}