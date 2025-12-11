package com.example.gametrack.data.remote.models

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)