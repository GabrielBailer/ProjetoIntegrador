package com.example.app_pi2

import androidx.room.*

@Dao
interface InteracaoDao {
    @Query("SELECT * FROM interacoes")
    suspend fun getAll(): List<Interacao>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interacao: Interacao)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(interacoes: List<Interacao>)

    @Query("DELETE FROM interacoes")
    suspend fun clearAll()
}
