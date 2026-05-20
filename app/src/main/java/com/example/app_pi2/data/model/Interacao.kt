package com.example.app_pi2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.app_pi2.utils.InteracaoCores

@Entity(tableName = "interacoes")
data class Interacao(
    @PrimaryKey val id: String = "",
    val titulo: String = "",
    val imagem: String? = null,
    val cor: String = "",
) {
    constructor() : this("", "", null, "")

    val corInt: Int
        get() = InteracaoCores.toColorInt(cor)
}