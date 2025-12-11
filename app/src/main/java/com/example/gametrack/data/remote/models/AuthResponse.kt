package com.example.gametrack.data.remote.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)