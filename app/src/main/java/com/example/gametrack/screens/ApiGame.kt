package com.example.gametrack.screens

import android.util.Log
import android.os.Handler
import android.os.Looper

object ApiGame {
    // SOLO imágenes locales - SIN IGDB, SIN RED, SIN OKHTTP
    private val localImages = mapOf(
        "minecraft" to "https://upload.wikimedia.org/wikipedia/en/5/51/Minecraft_cover.png",
        "fortnite" to "https://upload.wikimedia.org/wikipedia/en/3/31/Fortnite_cover_art.jpg",
        "zelda" to "https://upload.wikimedia.org/wikipedia/en/4/41/Legend_of_Zelda_Breath_of_the_Wild.jpg",
        "call of duty" to "https://upload.wikimedia.org/wikipedia/en/6/66/Call_of_Duty_Modern_Warefare_III.jpg",
        "pokemon" to "https://upload.wikimedia.org/wikipedia/en/4/49/Pok%C3%A9mon_Scarlet_and_Violet.jpg",
        "fifa" to "https://upload.wikimedia.org/wikipedia/en/8/8a/FIFA_23_Standard_Edition.jpg",
        "assassin" to "https://upload.wikimedia.org/wikipedia/en/5/5d/Assassin%27s_Creed_Mirage_cover.jpg",
        "elden ring" to "https://upload.wikimedia.org/wikipedia/en/b/b9/Elden_Ring_Box_art.jpg",
        "super mario" to "https://upload.wikimedia.org/wikipedia/en/8/8d/Super_Mario_Bros._Wonder_cover_art.jpg"
    )

    /**
     * VERSIÓN 100% SEGURA - solo busca en mapa local
     * NO hace llamadas de red, NO usa OkHttp, NO puede crashear
     */
    fun buscarCaratula(nombreJuego: String, callback: (String?) -> Unit) {
        Log.d("ApiGame", "🔍 BÚSQUEDA LOCAL SEGURA para: '$nombreJuego'")

        // Ejecutar en background thread con Handler
        Thread {
            try {
                Thread.sleep(500) // Simular delay de búsqueda

                if (nombreJuego.isBlank() || nombreJuego.length < 3) {
                    Log.d("ApiGame", "⚠️ Nombre muy corto")
                    Handler(Looper.getMainLooper()).post { callback(null) }
                    return@Thread
                }

                val nombreLower = nombreJuego.lowercase()
                var imagenEncontrada: String? = null

                // Buscar en el mapa local
                for ((keyword, url) in localImages) {
                    if (nombreLower.contains(keyword, ignoreCase = true)) {
                        imagenEncontrada = url
                        Log.d("ApiGame", "✅ Encontrado localmente: $keyword -> $url")
                        break
                    }
                }

                // Devolver al hilo principal
                Handler(Looper.getMainLooper()).post {
                    callback(imagenEncontrada)
                    Log.d("ApiGame", "🎯 Callback ejecutado: ${imagenEncontrada != null}")
                }

            } catch (e: Exception) {
                Log.e("ApiGame", "💥 ERROR (esto no debería pasar): ${e.message}")
                Handler(Looper.getMainLooper()).post { callback(null) }
            }
        }.start()
    }

    fun probarConexion(callback: (Boolean) -> Unit) {
        Log.d("ApiGame", "🔌 Test conexión (siempre true - modo local)")
        Handler(Looper.getMainLooper()).post { callback(true) }
    }
}
