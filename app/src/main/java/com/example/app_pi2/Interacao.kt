package com.example.app_pi2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interacoes")
data class Interacao(
    @PrimaryKey val id: String, // pode ser o UID do Firestore
    val titulo: String,
    val descricao: String
)
