package com.example.gametrack.data.remote.models

import com.google.gson.annotations.SerializedName

data class ForgotPasswordResponse(
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String? = null,
    @SerializedName("demo_mode") val demoMode: Boolean? = null
)