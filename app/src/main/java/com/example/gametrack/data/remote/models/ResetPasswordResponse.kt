package com.example.gametrack.data.remote.models

import com.google.gson.annotations.SerializedName

data class ResetPasswordResponse(
    @SerializedName("message") val message: String,
    @SerializedName("username") val username: String? = null
)