package com.example.app_pi2.utils

import com.example.app_pi2.data.model.Interacao
import java.util.UUID

object InteracoesPadrao {

    fun criarLista(): List<Interacao> {

        return listOf(

            Interacao(
                id = UUID.randomUUID().toString(),
                titulo = "Olá",
                imagem = "int_black_ola",
                cor = "#CFE8FF"
            ),

            Interacao(
                id = UUID.randomUUID().toString(),
                titulo = "Quero água",
                imagem = "int_black_agua",
                cor = "#D4F5DD"
            ),

            Interacao(
                id = UUID.randomUUID().toString(),
                titulo = "Estou com fome",
                imagem = "int_black_fome",
                cor = "#FFF3C4"
            ),

            Interacao(
                id = UUID.randomUUID().toString(),
                titulo = "Obrigado",
                imagem = "int_black_obrigado",
                cor = "#E3D4FF"
            )
        )
    }
}