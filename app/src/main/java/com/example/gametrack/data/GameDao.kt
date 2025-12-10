package com.example.gametrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE userId = :userId ORDER BY title ASC")
    fun getGamesForUser(userId: Int): Flow<List<Game>>

    @Query("SELECT * FROM games ORDER BY title ASC")
    fun getAllGames(): Flow<List<Game>>

    @Insert
    suspend fun insertGame(game: Game)

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("SELECT * FROM games WHERE title LIKE :query AND userId = :userId")
    fun searchGames(query: String, userId: Int): Flow<List<Game>>

    // 🆕 NUEVO: Método para debug
    @Query("SELECT COUNT(*) FROM games WHERE userId = :userId")
    suspend fun countGamesForUser(userId: Int): Int
}