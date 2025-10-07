package com.example.app_pi2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Interacao::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interacaoDao(): InteracaoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // para facilitar mudanças na versão
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
