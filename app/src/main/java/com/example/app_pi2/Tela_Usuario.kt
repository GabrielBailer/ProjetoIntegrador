package com.example.app_pi2

data class Usuario(
    val id: Int,
    var nomeResponsavel: String,
    var nomeUsuario: String,
    var nomeProfessor: String,
    var endereco: String,
    var email: String,
    var senha: String
) {
    override fun toString(): String {
        return "ID: $id | Usuário: $nomeUsuario | Resp: $nomeResponsavel"
    }
}