package com.example.app_pi2.data.local.DAO

import androidx.room.*
import com.example.app_pi2.data.model.Interacao

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

    @Query("SELECT COUNT(*) FROM interacoes")
    suspend fun countInteracoes(): Int

    @Query("SELECT * FROM interacoes WHERE id = :id LIMIT 1")
    fun getById(id: String): Interacao

    @Update
    fun update(interacao: Interacao)

    @Delete
    fun delete(interacao: Interacao)
}
