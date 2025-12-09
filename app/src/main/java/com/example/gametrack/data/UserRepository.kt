package com.example.gametrack.data

import android.content.Context
import android.util.Log
import com.example.gametrack.data.remote.api.ApiClient
import com.example.gametrack.data.remote.models.*
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class UserRepository(private val userDao: UserDao, private val context: Context) {

    private val NETWORK_TIMEOUT = 10000L

    // ==================== BCrypt METHODS ====================

    private fun hashPasswordBCrypt(password: String): String {
        return try {
            val hash = BCrypt.withDefaults().hashToString(10, password.toCharArray())
            Log.d("UserRepository", "🔐 Hash BCrypt generado (${hash.length} chars)")
            hash
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error BCrypt hashing: ${e.message}")
            throw e
        }
    }

    private fun verifyPasswordBCrypt(password: String, bcryptHash: String): Boolean {
        return try {
            val result = BCrypt.verifyer().verify(password.toCharArray(), bcryptHash.toCharArray())
            val isValid = result.verified
            Log.d("UserRepository", "🔍 Verificación BCrypt: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error BCrypt verification: ${e.message}")
            false
        }
    }

    // ==================== SHA-256 (PARA MIGRACIÓN) ====================

    private fun hashPasswordSHA256(password: String): String {
        return try {
            val bytes = password.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hash = digest.fold("") { str, it -> str + "%02x".format(it) }
            Log.d("UserRepository", "🔐 Hash SHA-256 generado: ${hash.take(10)}...")
            hash
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error SHA-256 hashing: ${e.message}")
            ""
        }
    }

    private fun isBCryptHash(hash: String): Boolean {
        // Los hash BCrypt siempre empiezan con $2a$, $2b$, $2y$ o similar
        return hash.startsWith("$2") && hash.length > 50
    }

    private fun isSHA256Hash(hash: String): Boolean {
        // SHA-256 son 64 caracteres hexadecimales
        return hash.length == 64 && hash.matches(Regex("^[a-fA-F0-9]{64}$"))
    }

    // ==================== LOGIN CON MIGRACIÓN ====================

    suspend fun loginUserHybrid(username: String, password: String): LoginResult {
        return try {
            Log.d("UserRepository", "🔐 LOGIN para: $username")

            // 1. Buscar usuario localmente
            val localUser = getUserByUsername(username)

            if (localUser != null) {
                Log.d("UserRepository", "📱 Usuario local encontrado")
                Log.d("UserRepository", "🔐 Hash almacenado: ${localUser.passHash.take(20)}...")
                Log.d("UserRepository", "🔐 Tipo de hash: ${detectHashType(localUser.passHash)}")

                // 2. DETECTAR tipo de hash y verificar
                when {
                    isBCryptHash(localUser.passHash) -> {
                        // Usuario ya migrado a BCrypt
                        Log.d("UserRepository", "🟢 Hash BCrypt detectado")
                        val passwordMatch = verifyPasswordBCrypt(password, localUser.passHash)

                        if (passwordMatch) {
                            Log.d("UserRepository", "✅ Login LOCAL (BCrypt)")
                            return LoginResult.SuccessLocal(localUser)
                        } else {
                            Log.d("UserRepository", "❌ Contraseña BCrypt incorrecta")
                        }
                    }

                    isSHA256Hash(localUser.passHash) -> {
                        // Usuario antiguo con SHA-256 - MIGRAR
                        Log.d("UserRepository", "🟡 Hash SHA-256 detectado - INICIANDO MIGRACIÓN")
                        val sha256Hash = hashPasswordSHA256(password)

                        if (sha256Hash == localUser.passHash) {
                            Log.d("UserRepository", "✅ Contraseña SHA-256 válida")

                            // MIGRAR a BCrypt
                            Log.d("UserRepository", "🔄 Migrando hash SHA-256 → BCrypt...")
                            val newBCryptHash = hashPasswordBCrypt(password)

                            // Actualizar en base de datos
                            val updatedUser = localUser.copy(passHash = newBCryptHash)
                            updateUser(updatedUser)

                            Log.d("UserRepository", "✅ Usuario migrado exitosamente")
                            Log.d("UserRepository", "🆕 Nuevo hash: ${newBCryptHash.take(20)}...")

                            return LoginResult.SuccessLocal(updatedUser)
                        } else {
                            Log.d("UserRepository", "❌ Contraseña SHA-256 incorrecta")
                        }
                    }

                    else -> {
                        // Hash desconocido o vacío
                        Log.e("UserRepository", "❌ Hash desconocido o vacío")
                    }
                }
            } else {
                Log.d("UserRepository", "📱 Usuario NO encontrado localmente")
            }

            // 3. Si no funciona localmente, intentar remoto
            Log.d("UserRepository", "🌐 Intentando login REMOTO...")
            val remoteResult = loginUserRemote(username, password)

            if (remoteResult.isSuccess) {
                val authResponse = remoteResult.getOrNull()
                Log.d("UserRepository", "✅ Login REMOTO exitoso")

                // Guardar usuario localmente (con BCrypt)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        saveUserLocal(username, password, authResponse?.email ?: "")
                    } catch (e: Exception) {
                        Log.e("UserRepository", "⚠️ Error guardando local: ${e.message}")
                    }
                }

                LoginResult.SuccessBackend(authResponse)
            } else {
                Log.d("UserRepository", "❌ Login remoto falló")
                LoginResult.Failure("Credenciales incorrectas")
            }

        } catch (e: Exception) {
            Log.e("UserRepository", "💥 ERROR en login: ${e.message}")
            LoginResult.Failure("Error: ${e.localizedMessage ?: "Desconocido"}")
        }
    }

    private fun detectHashType(hash: String): String {
        return when {
            isBCryptHash(hash) -> "BCrypt"
            isSHA256Hash(hash) -> "SHA-256"
            hash.isEmpty() -> "Vacío"
            else -> "Desconocido (${hash.length} chars)"
        }
    }

    private suspend fun loginUserRemote(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "📡 Conectando al backend...")
                val connectionOk = testConnectionBasic()
                if (!connectionOk) {
                    Log.e("UserRepository", "❌ Conexión básica falló")
                    return@withContext Result.failure(Exception("No hay conexión al servidor"))
                }

                val authService = ApiClient.getAuthApiService(context)
                val request = LoginRequest(username, password)
                Log.d("UserRepository", "➡️ Enviando login: $username")

                val response = withTimeout(NETWORK_TIMEOUT) {
                    authService.login(request)
                }

                Log.d("UserRepository", "📥 Respuesta HTTP: ${response.code()}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    Log.d("UserRepository", "📦 Body recibido: $authResponse")

                    if (authResponse != null && authResponse.username != null) {
                        Result.success(authResponse)
                    } else {
                        Result.failure(Exception("Datos inválidos en respuesta"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin mensaje"
                    Log.e("UserRepository", "❌ Error ${response.code()}: $errorBody")
                    Result.failure(Exception("Error ${response.code()}: $errorBody"))
                }

            } catch (e: TimeoutCancellationException) {
                Log.e("UserRepository", "⏰ Timeout en login remoto")
                Result.failure(Exception("Timeout: Servidor no responde"))
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error en login remoto: ${e.message}")
                Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
            }
        }
    }

    // ==================== REGISTER ====================

    suspend fun registerUserHybrid(username: String, password: String, email: String): RegisterResult {
        return try {
            Log.d("UserRepository", "📝 REGISTRO: $username")

            val existingUser = getUserByUsername(username)
            if (existingUser != null) {
                Log.d("UserRepository", "❌ Usuario ya existe localmente")
                return RegisterResult.Failure("El usuario ya existe")
            }

            Log.d("UserRepository", "🌐 Intentando registro remoto...")
            val remoteResult = registerUserRemote(username, password, email)

            if (remoteResult.isSuccess) {
                val authResponse = remoteResult.getOrNull()
                Log.d("UserRepository", "✅ Registro remoto exitoso")
                RegisterResult.SuccessBackend(authResponse)
            } else {
                Log.d("UserRepository", "💾 Guardando usuario localmente...")
                saveUserLocal(username, password, email)
                RegisterResult.SuccessLocal
            }

        } catch (e: Exception) {
            Log.e("UserRepository", "💥 ERROR en registro: ${e.message}")
            RegisterResult.Failure("Error: ${e.localizedMessage ?: "Desconocido"}")
        }
    }

    private suspend fun registerUserRemote(username: String, password: String, email: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "📡 Registro remoto para: $username")
                val authService = ApiClient.getAuthApiService(context)
                val request = RegisterRequest(username, email, password)

                val response = withTimeout(NETWORK_TIMEOUT) {
                    authService.register(request)
                }

                Log.d("UserRepository", "📥 Respuesta registro: ${response.code()}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null && authResponse.username != null) {
                        saveUserLocal(username, password, email)
                        Result.success(authResponse)
                    } else {
                        Result.failure(Exception("Datos inválidos en respuesta"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin mensaje"
                    Log.e("UserRepository", "❌ Error ${response.code()}: $errorBody")
                    val errorMsg = when (response.code()) {
                        409 -> "El usuario ya existe"
                        400 -> "Datos inválidos: $errorBody"
                        else -> "Error ${response.code()}: $errorBody"
                    }
                    Result.failure(Exception(errorMsg))
                }

            } catch (e: TimeoutCancellationException) {
                Log.e("UserRepository", "⏰ Timeout en registro")
                Result.failure(Exception("Timeout: Servidor no responde"))
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error en registro remoto: ${e.message}")
                Result.failure(Exception("Error de conexión"))
            }
        }
    }

    // ==================== LOCAL USER METHODS ====================

    private suspend fun saveUserLocal(username: String, password: String, email: String) {
        withContext(Dispatchers.IO) {
            try {
                // Siempre usar BCrypt para nuevos usuarios
                val passHash = hashPasswordBCrypt(password)

                val newUser = User(
                    username = username,
                    email = email,
                    passHash = passHash,
                    fotoPerfilUri = null
                )

                userDao.insertUser(newUser)
                Log.d("UserRepository", "💾 Usuario guardado localmente (BCrypt): $username")

            } catch (e: Exception) {
                Log.e("UserRepository", "⚠️ Error guardando usuario local: ${e.message}")
            }
        }
    }

    private suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
                Log.d("UserRepository", "✏️ Usuario actualizado: ${user.username}")
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error actualizando usuario: ${e.message}")
            }
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                userDao.getUserByUsername(username)
            } catch (e: Exception) {
                Log.e("UserRepository", "❌ Error obteniendo usuario: ${e.message}")
                null
            }
        }
    }

    // ==================== CONNECTION TEST ====================

    suspend fun testBackendConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()

                // ⬇️ TU IP AQUÍ TAMBIÉN
                val request = Request.Builder()
                    .url("http://10.116.67.176:8080/api/auth/test")
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun testConnectionBasic(): Boolean {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("http://10.116.67.176:8080/api/auth/test")  // 👈 Misma IP aquí
                .build()

            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            Log.d("UserRepository", "🔍 Test básico: $isSuccess (${response.code})")
            isSuccess
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Error test básico: ${e.message}")
            false
        }
    }

    // ==================== RESULT CLASSES ====================

    sealed class LoginResult {
        data class SuccessBackend(val authResponse: AuthResponse?) : LoginResult()
        data class SuccessLocal(val user: User) : LoginResult()
        data class Failure(val message: String) : LoginResult()
    }

    sealed class RegisterResult {
        data class SuccessBackend(val authResponse: AuthResponse?) : RegisterResult()
        object SuccessLocal : RegisterResult()
        data class Failure(val message: String) : RegisterResult()
    }
}
