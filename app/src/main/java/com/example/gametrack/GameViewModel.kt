package com.example.gametrack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametrack.data.Game
import com.example.gametrack.data.GameRepository
import com.example.gametrack.data.UserRepository
import com.example.gametrack.data.remote.models.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import kotlin.math.absoluteValue
import java.util.UUID

class GameViewModel(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // ========== ESTADOS ==========
    private var _currentUserId = 0
    private val _games = MutableStateFlow<List<Game>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    private val _currentUser = MutableStateFlow<UserState>(UserState.NotLoggedIn)
    private val _backendStatus = MutableStateFlow<BackendStatus>(BackendStatus.Unknown)

    // Flows públicos
    val games: StateFlow<List<Game>> = _games.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val currentUser: StateFlow<UserState> = _currentUser.asStateFlow()
    val backendStatus: StateFlow<BackendStatus> = _backendStatus.asStateFlow()

    // ========== SHARED PREFERENCES ==========
    private lateinit var context: Context

    // CLAVE: ID del usuario persistente
    private val USER_ID_PREF_KEY = "user_id"
    private val USERNAME_PREF_KEY = "username"
    private val EMAIL_PREF_KEY = "email"

    fun setContext(context: Context) {
        this.context = context
        loadUserIdFromPrefs()
    }

    private fun loadUserIdFromPrefs() {
        if (!this::context.isInitialized) return

        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        _currentUserId = prefs.getInt(USER_ID_PREF_KEY, 0)
        Log.d("GameViewModel", "📱 UserId cargado desde Prefs: $_currentUserId")
    }

    private fun saveUserToPrefs(userId: Int, username: String, email: String) {
        if (!this::context.isInitialized) return

        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt(USER_ID_PREF_KEY, userId)
            putString(USERNAME_PREF_KEY, username)
            putString(EMAIL_PREF_KEY, email)
            apply()
        }
        Log.d("GameViewModel", "💾 Guardado en Prefs: userId=$userId, username=$username, email=$email")
    }

    fun getUsernameFromPrefs(): String {
        if (!this::context.isInitialized) return ""
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString(USERNAME_PREF_KEY, "") ?: ""
    }

    fun getEmailFromPrefs(): String {
        if (!this::context.isInitialized) return ""
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString(EMAIL_PREF_KEY, "") ?: ""
    }

    private fun generateStableUserId(username: String): Int {
        // Genera un ID estable basado en el username
        return username.hashCode().absoluteValue % 1000000
    }

    init {
        Log.d("GameViewModel", "✅ ViewModel inicializado")
        checkBackendConnection()
    }

    // ========== CONEXIÓN BACKEND ==========
    private fun checkBackendConnection() {
        viewModelScope.launch {
            _backendStatus.value = BackendStatus.Checking
            try {
                val isConnected = testBackendConnection()
                _backendStatus.value = if (isConnected) {
                    BackendStatus.Connected
                } else {
                    BackendStatus.Disconnected
                }
                Log.d("GameViewModel", "🔗 Estado backend: ${_backendStatus.value}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error checkBackendConnection: ${e.message}")
                _backendStatus.value = BackendStatus.Disconnected
            }
        }
    }

    suspend fun testBackendConnection(): Boolean {
        return try {
            Log.d("GameViewModel", "🔌 Probando conexión backend...")
            val result = withContext(Dispatchers.IO) {
                try {
                    userRepository.testBackendConnection()
                } catch (e: Exception) {
                    Log.e("GameViewModel", "❌ Error en testBackendConnection: ${e.message}")
                    false
                }
            }
            Log.d("GameViewModel", "🔌 Resultado test: $result")
            result
        } catch (e: Exception) {
            Log.e("GameViewModel", "❌ Error testBackendConnection: ${e.message}")
            false
        }
    }

    // ========== AUTENTICACIÓN ==========
    suspend fun loginUser(username: String, password: String): Boolean {
        return try {
            Log.d("GameViewModel", "🔐 [LOGIN] Iniciando login para: $username")
            _authState.value = AuthState.Loading

            withContext(Dispatchers.IO) {
                try {
                    val result = userRepository.loginUserHybrid(username, password)
                    Log.d("GameViewModel", "📊 [LOGIN] Resultado: ${result::class.simpleName}")

                    when (result) {
                        is UserRepository.LoginResult.SuccessBackend -> {
                            val authResponse = result.authResponse
                            Log.d("GameViewModel", "✅ [LOGIN] Login backend exitoso")

                            // Obtener usuario de la base de datos
                            val userFromDb = userRepository.getUserByUsername(username)

                            _currentUser.value = if (userFromDb != null) {
                                UserState.LoggedInLocal(
                                    username = userFromDb.username,
                                    email = userFromDb.email
                                )
                            } else {
                                UserState.LoggedInLocal(
                                    username = username,
                                    email = authResponse?.email ?: ""
                                )
                            }

                            // GENERAR Y GUARDAR USER ID ESTABLE
                            _currentUserId = generateStableUserId(username)
                            val userEmail = if (userFromDb != null) {
                                userFromDb.email
                            } else {
                                authResponse?.email ?: ""
                            }

                            // Guardar en SharedPreferences
                            if (this@GameViewModel::context.isInitialized) {
                                saveUserToPrefs(_currentUserId, username, userEmail)
                            }

                            Log.d("GameViewModel", "🆔 [LOGIN] UserId asignado: $_currentUserId")

                            loadGamesForCurrentUser()
                            _authState.value = AuthState.SuccessBackend(authResponse)
                            true
                        }

                        is UserRepository.LoginResult.SuccessLocal -> {
                            Log.d("GameViewModel", "✅ [LOGIN] Login local exitoso")

                            val user = result.user
                            _currentUser.value = UserState.LoggedInLocal(
                                username = user.username,
                                email = user.email
                            )

                            // GENERAR Y GUARDAR USER ID ESTABLE
                            _currentUserId = generateStableUserId(user.username)

                            // Guardar en SharedPreferences
                            if (this@GameViewModel::context.isInitialized) {
                                saveUserToPrefs(_currentUserId, user.username, user.email)
                            }

                            Log.d("GameViewModel", "🆔 [LOGIN] UserId (local): $_currentUserId")

                            loadGamesForCurrentUser()
                            _authState.value = AuthState.SuccessLocal
                            true
                        }

                        is UserRepository.LoginResult.Failure -> {
                            Log.e("GameViewModel", "❌ [LOGIN] Login falló: ${result.message}")
                            _authState.value = AuthState.Error(result.message)
                            false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GameViewModel", "💥 [LOGIN] ERROR en userRepository: ${e.message}")
                    _authState.value = AuthState.Error("Error interno: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "💥 [LOGIN] ERROR CRÍTICO en loginUser: ${e.message}")
            _authState.value = AuthState.Error("Error crítico: ${e.message}")
            false
        }
    }

    suspend fun registerUser(username: String, password: String, email: String): Boolean {
        return try {
            Log.d("GameViewModel", "📝 [REGISTER] Iniciando registro para: $username")
            _authState.value = AuthState.Loading

            withContext(Dispatchers.IO) {
                try {
                    val result = userRepository.registerUserHybrid(username, password, email)
                    Log.d("GameViewModel", "📝 [REGISTER] Resultado: ${result::class.simpleName}")

                    when (result) {
                        is UserRepository.RegisterResult.SuccessBackend -> {
                            val authResponse = result.authResponse
                            Log.d("GameViewModel", "✅ [REGISTER] Registro backend exitoso")

                            _currentUser.value = if (authResponse != null) {
                                UserState.LoggedInBackend(
                                    username = authResponse.username ?: username,
                                    email = authResponse.email ?: email,
                                    displayName = authResponse.displayName ?: username
                                )
                            } else {
                                UserState.LoggedInLocal(username = username, email = email)
                            }

                            // GENERAR Y GUARDAR USER ID ESTABLE
                            _currentUserId = generateStableUserId(username)

                            // Guardar en SharedPreferences
                            if (this@GameViewModel::context.isInitialized) {
                                saveUserToPrefs(_currentUserId, username, email)
                            }

                            loadGamesForCurrentUser()
                            _authState.value = AuthState.SuccessBackend(authResponse)
                            true
                        }

                        is UserRepository.RegisterResult.SuccessLocal -> {
                            Log.d("GameViewModel", "✅ [REGISTER] Registro local exitoso")
                            _currentUser.value = UserState.LoggedInLocal(
                                username = username,
                                email = email
                            )

                            // GENERAR Y GUARDAR USER ID ESTABLE
                            _currentUserId = generateStableUserId(username)

                            // Guardar en SharedPreferences
                            if (this@GameViewModel::context.isInitialized) {
                                saveUserToPrefs(_currentUserId, username, email)
                            }

                            loadGamesForCurrentUser()
                            _authState.value = AuthState.SuccessLocal
                            true
                        }

                        is UserRepository.RegisterResult.Failure -> {
                            Log.e("GameViewModel", "❌ [REGISTER] Registro falló: ${result.message}")
                            _authState.value = AuthState.Error(result.message)
                            false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GameViewModel", "💥 [REGISTER] ERROR en userRepository: ${e.message}")
                    _authState.value = AuthState.Error("Error interno: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "💥 [REGISTER] ERROR CRÍTICO en registerUser: ${e.message}")
            _authState.value = AuthState.Error("Error crítico: ${e.message}")
            false
        }
    }

    // ========== JUEGOS ==========
    fun loadGamesForCurrentUser() {
        Log.d("GameViewModel", "🔄 [loadGamesForCurrentUser] UserId actual: $_currentUserId")

        if (_currentUserId <= 0) {
            Log.w("GameViewModel", "⚠️ UserId es 0, no se pueden cargar juegos")
            _games.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("GameViewModel", "📡 Cargando juegos para usuario: $_currentUserId")
                gameRepository.getGamesForUser(_currentUserId).collectLatest { gamesList ->
                    _games.value = gamesList
                    Log.d("GameViewModel", "📦 Juegos cargados: ${gamesList.size}")
                    gamesList.forEach { game ->
                        Log.d("GameViewModel", "   - ${game.title} (userId: ${game.userId})")
                    }
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error cargando juegos: ${e.message}")
                _errorMessage.value = "Error cargando juegos"
                _games.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addGame(game: Game) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val gameWithUser = game.copy(userId = _currentUserId)
                Log.d("GameViewModel", "➕ Añadiendo juego: ${game.title} con userId: $_currentUserId")

                gameRepository.insertGame(gameWithUser)

                // Recargar juegos después de añadir
                loadGamesForCurrentUser()

                _errorMessage.value = null
                Log.d("GameViewModel", "✅ Juego añadido exitosamente")
            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error añadiendo juego: ${e.message}")
                _errorMessage.value = "Error añadiendo juego: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("GameViewModel", "🗑️ Eliminando juego: ${game.title}")
                gameRepository.deleteGame(game)

                // Recargar juegos después de eliminar
                loadGamesForCurrentUser()

                _errorMessage.value = null
                Log.d("GameViewModel", "✅ Juego eliminado exitosamente")
            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error eliminando juego: ${e.message}")
                _errorMessage.value = "Error eliminando juego"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGame(updatedGame: Game) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val gameWithUser = updatedGame.copy(userId = _currentUserId)
                Log.d("GameViewModel", "✏️ Actualizando juego: ${updatedGame.title}")
                gameRepository.updateGame(gameWithUser)

                // Recargar juegos después de actualizar
                loadGamesForCurrentUser()

                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error actualizando juego: ${e.message}")
                _errorMessage.value = "Error actualizando juego"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== UTILIDADES ==========
    fun getCurrentUserId(): Int {
        Log.d("GameViewModel", "📱 [getCurrentUserId] devolviendo: $_currentUserId")
        return _currentUserId
    }

    fun getCurrentUsername(): String {
        return when (val state = _currentUser.value) {
            is UserState.LoggedInBackend -> state.username
            is UserState.LoggedInLocal -> state.username
            UserState.NotLoggedIn -> {
                // Intentar obtener de SharedPreferences
                if (this::context.isInitialized) {
                    getUsernameFromPrefs()
                } else {
                    ""
                }
            }
        }
    }

    fun getCurrentEmail(): String {
        return when (val state = _currentUser.value) {
            is UserState.LoggedInBackend -> state.email
            is UserState.LoggedInLocal -> state.email
            UserState.NotLoggedIn -> {
                // Intentar obtener de SharedPreferences
                if (this::context.isInitialized) {
                    getEmailFromPrefs()
                } else {
                    ""
                }
            }
        }
    }

    fun logout() {
        Log.d("GameViewModel", "🚪 Cerrando sesión")
        _authState.value = AuthState.Idle
        _currentUser.value = UserState.NotLoggedIn
        _currentUserId = 0
        _games.value = emptyList()

        // LIMPIAR SHAREDPREFERENCES
        if (this::context.isInitialized) {
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d("GameViewModel", "🧹 SharedPreferences limpiados")
        }
    }

    fun clearAuthState() {
        _authState.value = AuthState.Idle
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // ========== DEBUG ==========
    fun debugInfo(): String {
        return """
            UserId: $_currentUserId
            Username: ${getCurrentUsername()}
            Games loaded: ${_games.value.size}
            AuthState: ${_authState.value::class.simpleName}
        """.trimIndent()
    }

    // ========== CLASES SELLADAS ==========
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class SuccessBackend(val authResponse: AuthResponse?) : AuthState()
        object SuccessLocal : AuthState()
        data class Error(val message: String) : AuthState()
    }

    sealed class UserState {
        object NotLoggedIn : UserState()
        data class LoggedInBackend(
            val username: String,
            val email: String,
            val displayName: String
        ) : UserState()
        data class LoggedInLocal(
            val username: String,
            val email: String
        ) : UserState()
    }

    sealed class BackendStatus {
        object Unknown : BackendStatus()
        object Checking : BackendStatus()
        object Connected : BackendStatus()
        object Disconnected : BackendStatus()
    }
}