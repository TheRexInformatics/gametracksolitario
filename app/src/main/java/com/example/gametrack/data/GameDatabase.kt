package com.example.gametrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log

@Database(
    entities = [User::class, Game::class],
    version = 2,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            Log.d("GameDatabase", "📀 Obteniendo instancia de base de datos...")

            return INSTANCE ?: synchronized(this) {
                Log.d("GameDatabase", "🔨 Construyendo base de datos...")

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_track_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // ⚠️ TEMPORAL: Si hay error, borra y recrea
                    .build()

                INSTANCE = instance
                Log.d("GameDatabase", "✅ Base de datos creada exitosamente")
                instance
            }
        }

        // MIGRACIÓN de versión 1 a 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("GameDatabase", "🔄 Ejecutando migración 1→2")
                database.execSQL("ALTER TABLE games ADD COLUMN imagenUrl TEXT")
            }
        }
    }
}
