package com.example.app_pi2

data class Usuario(
    var nomeUsuario: String,
    var numeroContato: String,
    var email: String,
    var senha: String
) {
    override fun toString(): String {
        return "Usuário: $nomeUsuario"
    }
}