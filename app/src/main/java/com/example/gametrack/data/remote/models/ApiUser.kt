package com.example.gametrack.data.remote.models

import com.google.gson.annotations.SerializedName

data class ApiUser(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null
)