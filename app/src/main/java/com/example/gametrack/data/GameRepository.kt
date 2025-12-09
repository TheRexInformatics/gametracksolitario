package com.example.gametrack.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    fun getGamesForUser(userId: Int): Flow<List<Game>> =
        gameDao.getGamesForUser(userId)

    fun getAllGames(): Flow<List<Game>> =
        gameDao.getAllGames()

    suspend fun insertGame(game: Game) {
        gameDao.insertGame(game)
    }

    suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game)
    }

    suspend fun updateGame(game: Game) {
        gameDao.updateGame(game)
    }

    // NUEVO: Para actualizar solo la imagen si lo necesitas
    suspend fun updateGameImage(gameId: Int, imageUrl: String) {
        // Necesitarías una query específica en el DAO
        // O puedes obtener, modificar y actualizar:
        // val game = getGameById(gameId)
        // game?.copy(imagenUrl = imageUrl)?.let { updateGame(it) }
    }
}
