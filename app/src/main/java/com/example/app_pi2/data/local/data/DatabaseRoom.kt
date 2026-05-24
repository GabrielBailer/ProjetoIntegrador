package com.example.app_pi2.data.local.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.app_pi2.data.model.Interacao
import com.example.app_pi2.data.local.DAO.InteracaoDao

@Database(entities = [Interacao::class], version = 4)
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
                    // Atualizado para a versão não depreciada. 
                    // true indica que o Room pode apagar todas as tabelas se não encontrar uma migração.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
