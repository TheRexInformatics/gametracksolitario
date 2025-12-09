package com.example.gametrack.data.remote.models

import com.google.gson.annotations.SerializedName

data class TestResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String
)
