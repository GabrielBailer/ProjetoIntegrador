package com.example.app_pi2.data.model

data class Usuario(
    var nomeUsuario: String = "",
    var numeroContato: String = "",
    var email: String = ""
) {
    override fun toString(): String {
        return "Usuário: $nomeUsuario"
    }
}