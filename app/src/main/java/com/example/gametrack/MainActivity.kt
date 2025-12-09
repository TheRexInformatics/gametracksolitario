package com.example.gametrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.gametrack.data.remote.api.ApiClient
import com.example.gametrack.navigation.NavGraph
import com.example.gametrack.ui.theme.GameTrackTheme
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar handler global para crashes
        setupGlobalExceptionHandler()

        Log.d("MainActivity", "🚀 Iniciando aplicación...")

        // ✅ INICIALIZAR ApiClient PRIMERO
        try {
            ApiClient.init(this)
            Log.d("MainActivity", "✅ ApiClient inicializado")
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error inicializando ApiClient: ${e.message}")
        }

        setContent {
            GameTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    private fun setupGlobalExceptionHandler() {
        // Capturar excepciones no manejadas en threads
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AppCrash", "💥💥💥 APP CRASH DETECTADO 💥💥💥")
            Log.e("AppCrash", "💥 Hilo: ${thread.name}")
            Log.e("AppCrash", "💥 Excepción: ${throwable.javaClass.simpleName}")
            Log.e("AppCrash", "💥 Mensaje: ${throwable.message}")
            Log.e("AppCrash", "💥 Stack trace completo:")
            throwable.printStackTrace()

            // Terminar la app limpiamente
            finish()
        }
    }
}
