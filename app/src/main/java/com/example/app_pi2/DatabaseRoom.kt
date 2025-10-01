package com.example.app_pi2

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Interacao::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interacaoDao(): InteracaoDao
}
