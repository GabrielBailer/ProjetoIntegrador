package com.example.app_pi2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interacoes")
data class Interacao(
    @PrimaryKey val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val imagem: String? = null
) {
    constructor() : this("", "", "", null)
}