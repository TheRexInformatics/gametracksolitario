package com.example.gametrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val platform: String,
    val status: String,
    val rating: Int,
    val notes: String? = null,

    val userId: Int = 0,
    val imagenUrl: String? = null  // NUEVO CAMPO
)
