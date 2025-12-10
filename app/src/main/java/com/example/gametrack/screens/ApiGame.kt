package com.example.gametrack.screens

import android.util.Log
import android.os.Handler
import android.os.Looper
import okhttp3.*
import com.google.gson.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ApiGame {
    // ========== CONFIGURACIÓN IGDB ==========
    private const val CLIENT_ID = "2hvjsfjp9yx696o3mv28bl2c7j9xiy"
    private const val ACCESS_TOKEN = "bu6wlbtbkz04ddj1kam5k4eyb9o137"
    private const val BASE_URL = "https://api.igdb.com/v4/games"

    // ========== CACHE LOCAL COMO FALLBACK ==========
    private val localImages = mapOf(
        "minecraft" to "https://upload.wikimedia.org/wikipedia/en/5/51/Minecraft_cover.png",
        "fortnite" to "https://upload.wikimedia.org/wikipedia/en/3/31/Fortnite_cover_art.jpg",
        "zelda" to "https://upload.wikimedia.org/wikipedia/en/4/41/Legend_of_Zelda_Breath_of_the_Wild.jpg",
        "call of duty" to "https://upload.wikimedia.org/wikipedia/en/6/66/Call_of_Duty_Modern_Warefare_III.jpg",
        "pokemon" to "https://upload.wikimedia.org/wikipedia/en/4/49/Pok%C3%A9mon_Scarlet_and_Violet.jpg",
        "fifa" to "https://upload.wikimedia.org/wikipedia/en/8/8a/FIFA_23_Standard_Edition.jpg",
        "assassin" to "https://upload.wikimedia.org/wikipedia/en/5/5d/Assassin%27s_Creed_Mirage_cover.jpg",
        "elden ring" to "https://upload.wikimedia.org/wikipedia/en/b/b9/Elden_Ring_Box_art.jpg",
        "super mario" to "https://upload.wikimedia.org/wikipedia/en/8/8d/Super_Mario_Bros._Wonder_cover_art.jpg",
        "the witcher" to "https://upload.wikimedia.org/wikipedia/en/9/9f/The_Witcher_3_cover_art.jpg",
        "cyberpunk" to "https://upload.wikimedia.org/wikipedia/en/9/9f/Cyberpunk_2077_box_art.jpg",
        "red dead" to "https://upload.wikimedia.org/wikipedia/en/4/44/Red_Dead_Redemption_II.jpg",
        "god of war" to "https://upload.wikimedia.org/wikipedia/en/a/a7/God_of_War_2018_cover.jpg",
        "spider man" to "https://upload.wikimedia.org/wikipedia/en/e/e1/Spider-Man_PS4_cover.jpg",
        "resident evil" to "https://upload.wikimedia.org/wikipedia/en/f/fd/Resident_Evil_4_remake_cover.jpg"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * VERSIÓN MEJORADA: Primero intenta IGDB, luego fallback local
     */
    fun buscarCaratula(nombreJuego: String, callback: (String?) -> Unit) {
        if (nombreJuego.isBlank() || nombreJuego.length < 3) {
            Log.d("ApiGame", "⚠️ Nombre muy corto para búsqueda")
            Handler(Looper.getMainLooper()).post { callback(null) }
            return
        }

        Log.d("ApiGame", "🔍 BÚSQUEDA INICIADA para: '$nombreJuego'")

        // 1. PRIMERO: Intentar con IGDB (API real)
        buscarEnIGDB(nombreJuego) { igdbUrl ->
            if (igdbUrl != null) {
                Log.d("ApiGame", "✅ Encontrado en IGDB: $igdbUrl")
                Handler(Looper.getMainLooper()).post { callback(igdbUrl) }
            } else {
                Log.d("ApiGame", "❌ No encontrado en IGDB, intentando local...")
                // 2. SEGUNDO: Intentar con cache local
                val localUrl = buscarEnLocal(nombreJuego)
                Log.d("ApiGame", "🔄 Resultado local: ${if (localUrl != null) "Encontrado" else "No encontrado"}")
                Handler(Looper.getMainLooper()).post { callback(localUrl) }
            }
        }
    }

    /**
     * BÚSQUEDA EN IGDB (Internet Game Database)
     */
    private fun buscarEnIGDB(nombreJuego: String, callback: (String?) -> Unit) {
        Thread {
            try {
                Log.d("ApiGame", "🌐 Consultando IGDB API...")

                val query = "search \"$nombreJuego\"; fields name, cover.image_id; limit 1;"
                val request = Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Client-ID", CLIENT_ID)
                    .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
                    .post(query.toRequestBody("text/plain".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()

                Log.d("ApiGame", "📡 Respuesta IGDB: ${response.code}")

                if (!response.isSuccessful) {
                    Log.w("ApiGame", "⚠️ IGDB error HTTP: ${response.code}")
                    callback(null)
                    return@Thread
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    Log.w("ApiGame", "⚠️ IGDB respuesta vacía")
                    callback(null)
                    return@Thread
                }

                Log.d("ApiGame", "📦 IGDB raw: ${responseBody.take(200)}...")

                val jsonArray = try {
                    gson.fromJson(responseBody, JsonArray::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.e("ApiGame", "❌ Error parsing IGDB JSON: ${e.message}")
                    null
                }

                if (jsonArray == null || jsonArray.size() == 0) {
                    Log.d("ApiGame", "ℹ️ IGDB: No se encontraron juegos")
                    callback(null)
                    return@Thread
                }

                val game = jsonArray[0].asJsonObject
                val coverElement = game["cover"]

                if (coverElement == null || coverElement.isJsonNull) {
                    Log.d("ApiGame", "ℹ️ IGDB: Juego encontrado pero sin carátula")
                    callback(null)
                    return@Thread
                }

                val coverId = try {
                    coverElement.asJsonObject["image_id"]?.asString
                } catch (e: Exception) {
                    Log.e("ApiGame", "❌ Error obteniendo cover_id: ${e.message}")
                    null
                }

                val imageUrl = coverId?.let { id ->
                    "https://images.igdb.com/igdb/image/upload/t_cover_big/$id.jpg"
                }

                if (imageUrl != null) {
                    Log.d("ApiGame", "🖼️ IGDB URL generada: $imageUrl")
                }

                callback(imageUrl)

            } catch (e: IOException) {
                Log.e("ApiGame", "🌐 Error de red IGDB: ${e.message}")
                callback(null)
            } catch (e: Exception) {
                Log.e("ApiGame", "💥 Error inesperado IGDB: ${e.message}")
                callback(null)
            }
        }.start()
    }

    /**
     * BÚSQUEDA EN CACHE LOCAL (fallback)
     */
    private fun buscarEnLocal(nombreJuego: String): String? {
        val nombreLower = nombreJuego.lowercase()

        // Intentar coincidencia exacta primero
        for ((keyword, url) in localImages) {
            if (nombreLower.contains(keyword, ignoreCase = true)) {
                Log.d("ApiGame", "✅ Coincidencia local: $keyword")
                return url
            }
        }

        // Si no hay coincidencia exacta, buscar por palabras
        val palabras = nombreLower.split(" ", "-", ":")
        for (palabra in palabras) {
            if (palabra.length < 4) continue

            for ((keyword, url) in localImages) {
                if (keyword.contains(palabra, ignoreCase = true)) {
                    Log.d("ApiGame", "✅ Coincidencia parcial: '$palabra' en '$keyword'")
                    return url
                }
            }
        }

        return null
    }

    /**
     * PRUEBA DE CONEXIÓN A IGDB
     */
    fun probarConexionIGDB(callback: (Boolean) -> Unit) {
        Thread {
            try {
                Log.d("ApiGame", "🔌 Probando conexión a IGDB...")

                val testQuery = "fields name; limit 1;"
                val request = Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Client-ID", CLIENT_ID)
                    .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
                    .post(testQuery.toRequestBody("text/plain".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val isConnected = response.isSuccessful

                Log.d("ApiGame", "📡 Test IGDB: ${if (isConnected) "✅ CONECTADO" else "❌ FALLÓ (${response.code})"}")

                Handler(Looper.getMainLooper()).post { callback(isConnected) }

            } catch (e: Exception) {
                Log.e("ApiGame", "💥 Error test IGDB: ${e.message}")
                Handler(Looper.getMainLooper()).post { callback(false) }
            }
        }.start()
    }
}