package com.example.app_pi2.utils

import android.graphics.Color

object InteracaoCores {

    const val VERMELHO = "#FFC9C9"
    const val LARANJA = "#FFD8B2"
    const val AMARELO = "#FFF3C4"
    const val VERDE = "#D4F5DD"
    const val AZUL = "#CFE8FF"
    const val ROXO = "#E3D4FF"

    fun toColorInt(cor: String): Int {

        return try {

            Color.parseColor(cor)

        } catch (_: Exception) {

            Color.WHITE
        }
    }
}