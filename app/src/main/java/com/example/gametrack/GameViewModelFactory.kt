package com.example.gametrack

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gametrack.data.*
import com.example.gametrack.data.remote.api.ApiClient

class GameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("ViewModelFactory", "🔧 Creando GameViewModel...")

        try {
            // 1. INICIALIZAR ApiClient (POR SI ACASO)
            ApiClient.init(context)
            Log.d("ViewModelFactory", "✅ ApiClient inicializado")

            // 2. OBTENER DATABASE
            val database = GameDatabase.getDatabase(context)
            Log.d("ViewModelFactory", "✅ Database obtenida")

            // 3. OBTENER DAOs
            val gameDao = database.gameDao()
            val userDao = database.userDao()
            Log.d("ViewModelFactory", "✅ DAOs obtenidos")

            // 4. CREAR REPOSITORIOS
            val gameRepository = GameRepository(gameDao)
            val userRepository = UserRepository(userDao, context)
            Log.d("ViewModelFactory", "✅ Repositorios creados")

            // 5. CREAR ViewModel
            val viewModel = GameViewModel(gameRepository, userRepository)

            // 🆕 PASAR EL CONTEXTO AL VIEWMODEL
            viewModel.setContext(context)

            Log.d("ViewModelFactory", "✅ GameViewModel creado exitosamente")

            return viewModel as T

        } catch (e: Exception) {
            Log.e("ViewModelFactory", "💥 ERROR creando ViewModel: ${e.message}")
            Log.e("ViewModelFactory", "Stack trace: ${e.stackTraceToString()}")
            throw RuntimeException("Error creando ViewModel: ${e.message}", e)
        }
    }
}