package com.example.gametrack.data.remote.models

import com.google.gson.annotations.SerializedName

data class UsernameAvailable(
    @SerializedName("available") val available: Boolean
)
