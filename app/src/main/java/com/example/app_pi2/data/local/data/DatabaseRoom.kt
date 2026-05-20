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
                    // fallbackToDestructiveMigration irá recriar o banco de dados se a versão mudar.
                    // Isso é útil durante o desenvolvimento, mas para um aplicativo em produção,
                    // você precisaria implementar uma migração real.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
