package com.example.gametrack.data.remote.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.gametrack.BuildConfig

object ApiClient {

    private const val BASE_URL = "http://10.116.67.176:8080/api/"

    private var retrofit: Retrofit? = null
    private var authApiService: AuthApiService? = null

    fun init(context: Context) {
        Log.d("ApiClient", "⚡ INICIALIZANDO API CLIENT")
        Log.d("ApiClient", "📍 URL: $BASE_URL")
        getRetrofit()
    }

    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            if (BuildConfig.DEBUG) {
                Log.d("API", message)
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("ApiClient", "➡️ ${request.method} ${request.url}")

                val newRequest = request.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build()

                try {
                    val response = chain.proceed(newRequest)
                    Log.d("ApiClient", "⬅️ ${response.code} ${response.message}")
                    response
                } catch (e: Exception) {
                    Log.e("ApiClient", "💥 Error de red: ${e.message}")
                    throw e
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    try {
                        retrofit = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(getOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create(
                                GsonBuilder()
                                    .setLenient()
                                    .create()
                            ))
                            .build()
                        Log.d("ApiClient", "✅ Retrofit creado")
                    } catch (e: Exception) {
                        Log.e("ApiClient", "💥 Error creando Retrofit: ${e.message}")
                        throw e
                    }
                }
            }
        }
        return retrofit!!
    }

    fun getAuthApiService(context: Context): AuthApiService {
        if (authApiService == null) {
            synchronized(this) {
                if (authApiService == null) {
                    authApiService = getRetrofit().create(AuthApiService::class.java)
                    Log.d("ApiClient", "✅ AuthApiService creado")
                }
            }
        }
        return authApiService!!
    }

    fun logCurrentConfig() {
        Log.d("ApiClient", "=== CONFIG ===")
        Log.d("ApiClient", "URL: $BASE_URL")
        Log.d("ApiClient", "Retrofit: ${retrofit != null}")
        Log.d("ApiClient", "Service: ${authApiService != null}")
        Log.d("ApiClient", "==============")
    }
}
